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
    private MongoTemplate mongoTemplate;

    public MongodbRepository(MongoTemplate mongoTemplate) {
        this(mongoTemplate, Long.MAX_VALUE);
    }

    public MongodbRepository(MongoTemplate mongoTemplate, long maxLockTime) {
        this(mongoTemplate, new MongodbStore<>(mongoTemplate), new MongodbMutexes<>(mongoTemplate, maxLockTime));
        ((MongodbMutexes<ID>) mutexes).setCollectionName(entityType);
    }

    public MongodbRepository(MongoTemplate mongoTemplate, Mutexes<ID> mutexes) {
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
        return mongoTemplate.count(new Query(), entityClass);
    }

    public List<E> queryAllByField(String fieldName, Object fieldValue) {
        Query query = query(where(fieldName).is(fieldValue));
        return mongoTemplate.find(query, entityClass);
    }

    public List<ID> queryAllIds() {
        Query query = new Query();
        query.fields().include();
        List<E> entityList = mongoTemplate.find(query, entityClass);
        List<ID> idList = new ArrayList<>();
        for (E entity : entityList) {
            idList.add(getId(entity));
        }
        return idList;
    }

}
