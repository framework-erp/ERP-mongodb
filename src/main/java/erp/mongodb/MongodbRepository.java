package erp.mongodb;

import erp.AppContext;
import erp.repository.Repository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongodbRepository<E, ID> extends Repository<E, ID> {
    protected String collectionName;
    protected MongoTemplate mongoTemplate;

    protected MongodbRepository(MongoTemplate mongoTemplate) {
        this.collectionName = entityType.getSimpleName();
        this.store = new MongodbStore<>(mongoTemplate, entityType, entityIDField, collectionName);
        this.mutexes = new MongodbMutexes(mongoTemplate, collectionName, 30000L);
        this.mongoTemplate = mongoTemplate;
        AppContext.registerRepository(this);
    }

    protected MongodbRepository(MongoTemplate mongoTemplate, String repositoryName) {
        super(repositoryName);
        this.collectionName = repositoryName;
        this.store = new MongodbStore<>(mongoTemplate, entityType, entityIDField, collectionName);
        this.mutexes = new MongodbMutexes(mongoTemplate, collectionName, 30000L);
        this.mongoTemplate = mongoTemplate;
        AppContext.registerRepository(this);
    }

    public MongodbRepository(MongoTemplate mongoTemplate, Class<E> entityClass) {
        super(entityClass);
        this.collectionName = entityClass.getSimpleName();
        this.store = new MongodbStore<>(mongoTemplate, entityClass, entityIDField, collectionName);
        this.mutexes = new MongodbMutexes(mongoTemplate, collectionName, 30000L);
        this.mongoTemplate = mongoTemplate;
        AppContext.registerRepository(this);
    }

    public MongodbRepository(MongoTemplate mongoTemplate, Class<E> entityClass, String repositoryName) {
        super(entityClass, repositoryName);
        this.collectionName = repositoryName;
        this.store = new MongodbStore<>(mongoTemplate, entityClass, entityIDField, collectionName);
        this.mutexes = new MongodbMutexes(mongoTemplate, collectionName, 30000L);
        this.mongoTemplate = mongoTemplate;
        AppContext.registerRepository(this);
    }

    public long count() {
        if (mongoTemplate == null) {
            return 0;
        }
        return mongoTemplate.count(new Query(), collectionName);
    }

    public List<E> queryAllByField(String fieldName, Object fieldValue) {
        if (mongoTemplate == null) {
            return null;
        }
        Query query = query(where(fieldName).is(fieldValue));
        return mongoTemplate.find(query, entityType, collectionName);
    }

    public List<ID> queryAllIds() {
        if (mongoTemplate == null) {
            return null;
        }
        Query query = new Query();
        String docKeyName;
        if (entityIDField.equals("id")) {
            docKeyName = "_id";
        } else {
            docKeyName = entityIDField;
        }
        query.fields().include(docKeyName);
        List<ID> idList = new ArrayList<>();
        List<Map> results = mongoTemplate.find(query, Map.class, collectionName);
        for (Map result : results) {
            idList.add((ID) result.get(docKeyName));
        }
        return idList;
    }

    public List<E> queryAllEntities() {
        if (mongoTemplate == null) {
            return null;
        }
        Query query = new Query();
        List<E> entityList = mongoTemplate.find(query, entityType, collectionName);
        return entityList;
    }

    public String getCollectionName() {
        return collectionName;
    }
}
