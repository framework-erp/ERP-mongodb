package erp.mongodb;

import erp.process.ProcessEntity;
import erp.repository.Store;
import erp.repository.impl.mem.MemStore;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

import java.util.Map;
import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongodbStore<E, ID> implements Store<E, ID> {

    private MongoTemplate mongoTemplate;

    private MemStore<E, ID> mockStore;

    private Class<E> entityClass;

    private String collectionName;

    private String docKeyName;

    public MongodbStore(MongoTemplate mongoTemplate, Class<E> entityClass, String idFieldName, String collectionName) {
        if (mongoTemplate == null) {
            initAsMock();
            return;
        }
        this.mongoTemplate = mongoTemplate;
        this.entityClass = entityClass;
        this.collectionName = collectionName;

        if (idFieldName.equals("id")) {
            docKeyName = "_id";
        } else {
            docKeyName = idFieldName;
            //创建索引
            IndexOperations indexOps = mongoTemplate.indexOps(collectionName);
            Index index = new Index().on(idFieldName, Sort.Direction.ASC).unique();
            indexOps.ensureIndex(index);
        }

    }

    private void initAsMock() {
        mockStore = new MemStore<E, ID>();
    }

    @Override
    public E load(ID id) {
        if (isMock()) {
            return mockStore.load(id);
        }
        return mongoTemplate.findOne(query(where(docKeyName).is(id)), entityClass, collectionName);
    }

    private boolean isMock() {
        return mockStore != null;
    }

    @Override
    public void insert(ID id, E entity) {
        if (isMock()) {
            mockStore.insert(id, entity);
            return;
        }
        mongoTemplate.insert(entity, collectionName);
    }

    @Override
    public void saveAll(Map<Object, Object> entitiesToInsert, Map<Object, ProcessEntity> entitiesToUpdate) {
        if (isMock()) {
            mockStore.saveAll(entitiesToInsert, entitiesToUpdate);
            return;
        }
        if (entitiesToInsert != null) {
            mongoTemplate.insert(entitiesToInsert.values(), collectionName);
        }
        if (entitiesToUpdate != null) {
            BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, collectionName);
            for (Map.Entry<Object, ProcessEntity> entry : entitiesToUpdate.entrySet()) {
                bulkOps.replaceOne(query(where(docKeyName).is(entry.getKey())), entry.getValue().getEntity());
            }
            bulkOps.execute();
        }
    }

    @Override
    public void removeAll(Set<Object> ids) {
        if (isMock()) {
            mockStore.removeAll(ids);
            return;
        }
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, collectionName);
        for (Object id : ids) {
            bulkOps.remove(query(where(docKeyName).is(id)));
        }
        bulkOps.execute();
    }

    public void setEntityClass(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

}
