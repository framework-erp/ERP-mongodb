package erp.mongodb;

import erp.process.ProcessEntity;
import erp.repository.Store;
import erp.repository.impl.mem.MemStore;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Map;
import java.util.Set;

public class MongodbStore<E, ID> implements Store<E, ID> {

    private MongoTemplate mongoTemplate;

    private MemStore<E, ID> mockStore;

    private Class<E> entityClass;

    public MongodbStore(MongoTemplate mongoTemplate) {
        if (mongoTemplate == null) {
            initAsMock();
            return;
        }
        this.mongoTemplate = mongoTemplate;
    }

    public MongodbStore(MongoTemplate mongoTemplate, Class<E> entityClass) {
        this(mongoTemplate);
        this.entityClass = entityClass;
    }

    private void initAsMock() {
        mockStore = new MemStore<E, ID>();
    }

    @Override
    public E load(ID id) {
        if (isMock()) {
            return mockStore.load(id);
        }
        return mongoTemplate.findById(id, entityClass);
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
        mongoTemplate.save(entity);
    }

    @Override
    public void saveAll(Map<Object, Object> entitiesToInsert, Map<Object, ProcessEntity> entitiesToUpdate) {
        if (isMock()) {
            mockStore.saveAll(entitiesToInsert, entitiesToUpdate);
            return;
        }
        if (entitiesToInsert != null) {
            mongoTemplate.insert(entitiesToInsert.values(), entityClass);
        }
        if (entitiesToUpdate != null) {
            for (ProcessEntity processEntity : entitiesToUpdate.values()) {
                mongoTemplate.save(processEntity.getEntity());
            }
        }
    }

    @Override
    public void removeAll(Set<Object> ids) {
        if (isMock()) {
            mockStore.removeAll(ids);
            return;
        }
        for (Object id : ids) {
            E entity = load((ID) id);
            if (entity != null) {
                mongoTemplate.remove(entity);
            }
        }
    }

    public void setEntityClass(Class<E> entityClass) {
        this.entityClass = entityClass;
    }
}
