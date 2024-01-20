package erp.mongodb;

import erp.process.ProcessEntity;
import erp.repository.Store;
import erp.repository.impl.mem.MemStore;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongodbStore<E, ID> implements Store<E, ID> {

    private MongoTemplate mongoTemplate;

    private MemStore<E, ID> mockStore;

    private Class<E> entityClass;

    private String docKeyName;

    public MongodbStore(MongoTemplate mongoTemplate, Class<E> entityClass) {
        if (mongoTemplate == null) {
            initAsMock();
            return;
        }
        this.mongoTemplate = mongoTemplate;
        this.entityClass = entityClass;

        //取名称为“id”的field作为id field，如果不存在 “id” field，那么取第一个field作为id field
        Field idField = null;
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("id")) {
                idField = field;
                break;
            }
        }
        if (idField == null) {
            if (fields.length > 0) {
                idField = fields[0];
            } else {
                throw new RuntimeException("can not find id field in entity class " + entityClass.getName());
            }
        }
        String idFieldName = idField.getName();
        if (idFieldName.equals("id")) {
            docKeyName = "_id";
        } else {
            docKeyName = idFieldName;
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
        return mongoTemplate.findOne(query(where(docKeyName).is(id)), entityClass);
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
        mongoTemplate.insert(entity);
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
            for (Map.Entry<Object, ProcessEntity> entry : entitiesToUpdate.entrySet()) {
                mongoTemplate.findAndReplace(query(where(docKeyName).is(entry.getKey())), entry.getValue().getEntity());
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
                mongoTemplate.remove(query(where(docKeyName).is(id)), entityClass);
            }
        }
    }

    public void setEntityClass(Class<E> entityClass) {
        this.entityClass = entityClass;
    }
}
