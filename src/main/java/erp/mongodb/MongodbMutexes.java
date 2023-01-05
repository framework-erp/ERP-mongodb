package erp.mongodb;

import erp.repository.Mutexes;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Set;

/**
 * @author zheng chengdong
 */
public class MongodbMutexes<ID> implements Mutexes<ID> {

    private MongoTemplate mongoTemplate;

    private boolean mock;

    private String entityType;

    public MongodbMutexes(MongoTemplate mongoTemplate, String entityType) {
        if (mongoTemplate == null) {
            mock = true;
            return;
        }
        this.mongoTemplate = mongoTemplate;
        this.entityType = entityType;
    }

    @Override
    public int lock(ID id, String processName) {
        return 0;
    }

    @Override
    public boolean newAndLock(ID id, String processName) {
        return false;
    }

    @Override
    public void unlockAll(Set<Object> ids) {

    }

    @Override
    public String getLockProcess(ID id) {
        return null;
    }

    @Override
    public void removeAll(Set<Object> ids) {

    }
}
