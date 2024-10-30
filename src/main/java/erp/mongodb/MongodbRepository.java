package erp.mongodb;

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

    public MongodbRepository(MongoTemplate mongoTemplate, Class<E> entityClass) {
        super(entityClass.getName());
        this.store = new MongodbStore<>(mongoTemplate, entityClass);
        this.mutexes = new MongodbMutexes(mongoTemplate, entityClass.getName(), 30000L);
        this.mongoTemplate = mongoTemplate;
        this.entityClass = entityClass;
    }

    protected MongodbRepository(MongoTemplate mongoTemplate) {
        try {
            this.entityClass = (Class<E>) Class.forName(entityType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("can not parse entity type", e);
        }
        this.store = new MongodbStore<>(mongoTemplate, entityClass);
        this.mutexes = new MongodbMutexes(mongoTemplate, entityType, 30000L);
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
