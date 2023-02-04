package erp.mongodb.interfaceimplementer;

import erp.mongodb.MongodbRepository;
import erp.repository.Mutexes;
import org.springframework.data.mongodb.core.MongoTemplate;

public class TemplateEntityRepositoryImpl extends MongodbRepository<TemplateEntity, Object> implements TemplateEntityRepository {
    public TemplateEntityRepositoryImpl(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    public TemplateEntityRepositoryImpl(MongoTemplate mongoTemplate, long maxLockTime) {
        super(mongoTemplate, maxLockTime);
    }

    public TemplateEntityRepositoryImpl(MongoTemplate mongoTemplate, Mutexes<Object> mutexes) {
        super(mongoTemplate, mutexes);
    }
}
