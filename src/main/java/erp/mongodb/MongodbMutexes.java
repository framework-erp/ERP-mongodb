package erp.mongodb;

import erp.repository.Mutexes;

import java.util.Set;

/**
 * @author zheng chengdong
 */
public class MongodbMutexes<ID> implements Mutexes<ID> {
    @Override
    public boolean exists(ID id) {
        "mutexes_"
        String collectionName = "arp_repo_state_" + ettCls.getSimpleName();
        Document cmd = new Document();
        cmd.put("findAndModify", collectionName);
        Document query = new Document();
        query.put("_id", id);
        return false;
    }

    @Override
    public int lock(ID id, String s) {
        return 0;
    }

    @Override
    public boolean newAndLock(ID id, String s) {
        return false;
    }

    @Override
    public void unlockAll(Set<Object> set) {

    }

    @Override
    public String getLockProcess(ID id) {
        return null;
    }

    @Override
    public void removeAll(Set<Object> set) {

    }
}
