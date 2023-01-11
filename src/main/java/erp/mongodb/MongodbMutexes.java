package erp.mongodb;

import erp.repository.Mutexes;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author zheng chengdong
 */
public class MongodbMutexes<ID> implements Mutexes<ID> {

    private MongoTemplate mongoTemplate;

    private boolean mock;

    private String collectionName;

    private long maxLockTime;

    private CurrentTimeMillisClock clock = CurrentTimeMillisClock.getInstance();

    public MongodbMutexes(MongoTemplate mongoTemplate, long maxLockTime) {
        if (mongoTemplate == null) {
            mock = true;
            return;
        }
        this.mongoTemplate = mongoTemplate;
        this.maxLockTime = maxLockTime;
    }

    public void setCollectionName(String entityType) {
        this.collectionName = "mutexes_" + entityType;
    }

    @Override
    public int lock(ID id, String processName) {
        if (mock) {
            return 1;
        }
        long currTime = clock.now();
        long unlockTime = currTime - maxLockTime;
        Query query = query(new Criteria().andOperator(where("id").is(id),
                new Criteria().orOperator(where("lock").is(false), where("lockTime").lt(unlockTime))));
        Update update = new Update().set("lock", true).set("lockTime", currTime).set("lockProcess", processName);
        Mutex<ID> mutex = mongoTemplate.findAndModify(query, update, Mutex.class, collectionName);
        if (mutex != null) {
            return 1;
        }
        boolean exists = mongoTemplate.exists(query(where("_id").is(id)), collectionName);
        if (!exists) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean newAndLock(ID id, String processName) {
        if (mock) {
            return true;
        }
        Mutex<ID> mutex = new Mutex<>();
        mutex.setId(id);
        mutex.setLock(true);
        mutex.setLockTime(clock.now());
        mutex.setLockProcess(processName);
        try {
            mongoTemplate.insert(mutex, collectionName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void unlockAll(Set<Object> ids) {
        if (mock) {
            return;
        }
        for (Object id : ids) {
            Query query = query(where("_id").is(id));
            Update update = new Update();
            update.set("lock", false);
            mongoTemplate.updateFirst(query, update, collectionName);
        }
    }

    @Override
    public String getLockProcess(ID id) {
        if (mock) {
            return null;
        }
        Mutex<ID> mutex = mongoTemplate.findById(id, Mutex.class, collectionName);
        if (mutex == null) {
            return null;
        }
        return mutex.getLockProcess();
    }

    @Override
    public void removeAll(Set<Object> ids) {
        if (mock) {
            return;
        }
        for (Object id : ids) {
            Query query = query(where("_id").is(id));
            mongoTemplate.remove(query, collectionName);
        }
    }
}
