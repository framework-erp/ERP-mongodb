package erp.mongodb;

import erp.repository.Mutexes;
import erp.repository.Repository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongodbRepository<E, ID> extends Repository<E, ID> {
    private Class<E> entityClass;
    protected MongoTemplate mongoTemplate;

    public MongodbRepository(MongoTemplate mongoTemplate, Class<E> entityType) {
        this(mongoTemplate, 30000, entityType);
    }

    public MongodbRepository(MongoTemplate mongoTemplate, long maxLockTime, Class<E> entityType) {
        this(mongoTemplate, new MongodbStore<>(mongoTemplate), new MongodbMutexes<>(mongoTemplate, maxLockTime), entityType);
        ((MongodbMutexes<ID>) mutexes).setCollectionName(entityType.getName());
    }

    public MongodbRepository(MongoTemplate mongoTemplate, Mutexes<ID> mutexes, Class<E> entityType) {
        this(mongoTemplate, new MongodbStore<>(mongoTemplate), mutexes, entityType);
    }

    private MongodbRepository(MongoTemplate mongoTemplate, MongodbStore<E, ID> store, Mutexes<ID> mutexes, Class<E> entityType) {
        super(store, mutexes, entityType.getName());
        this.entityClass = entityType;
        store.setEntityClass(entityClass);
        this.mongoTemplate = mongoTemplate;
    }

    protected MongodbRepository(MongoTemplate mongoTemplate) {
        this(mongoTemplate, 30000);
    }

    protected MongodbRepository(MongoTemplate mongoTemplate, long maxLockTime) {
        this(mongoTemplate, new MongodbStore<>(mongoTemplate), new MongodbMutexes<>(mongoTemplate, maxLockTime));
        ((MongodbMutexes<ID>) mutexes).setCollectionName(entityType);
    }

    protected MongodbRepository(MongoTemplate mongoTemplate, Mutexes<ID> mutexes) {
        this(mongoTemplate, new MongodbStore<>(mongoTemplate), mutexes);
    }

    private MongodbRepository(MongoTemplate mongoTemplate, MongodbStore<E, ID> store, Mutexes<ID> mutexes) {
        super(store, mutexes);
        try {
            this.entityClass = (Class<E>) Class.forName(entityType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("can not parse entity type", e);
        }
        store.setEntityClass(entityClass);
        this.mongoTemplate = mongoTemplate;
    }

    public long count() {
        if (mongoTemplate == null) {
            return 0;
        }
        return mongoTemplate.count(new Query(), entityClass);
    }

    public List<E> queryAllByField(String fieldName, Object fieldValue) {
        if (mongoTemplate == null) {
            return null;
        }
        Query query = query(where(fieldName).is(fieldValue));
        return mongoTemplate.find(query, entityClass);
    }

    public List<ID> queryAllIds() {
        if (mongoTemplate == null) {
            return null;
        }
        Query query = new Query();
        query.fields().include();
        List<E> entityList = mongoTemplate.find(query, entityClass);
        List<ID> idList = new ArrayList<>();
        for (E entity : entityList) {
            idList.add(getId(entity));
        }
        return idList;
    }

    public List<E> queryAllEntities() {
        if (mongoTemplate == null) {
            return null;
        }
        Query query = new Query();
        List<E> entityList = mongoTemplate.find(query, entityClass);
        return entityList;
    }

}
