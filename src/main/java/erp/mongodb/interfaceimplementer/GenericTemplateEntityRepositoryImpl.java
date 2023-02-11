package erp.mongodb.interfaceimplementer;

import erp.mongodb.MongodbRepository;
import erp.repository.Mutexes;
import org.springframework.data.mongodb.core.MongoTemplate;

public class GenericTemplateEntityRepositoryImpl extends MongodbRepository<TemplateEntityImpl, Object> implements GenericTemplateEntityRepository<TemplateEntityImpl, Object> {

    public GenericTemplateEntityRepositoryImpl(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    public GenericTemplateEntityRepositoryImpl(MongoTemplate mongoTemplate, long maxLockTime) {
        super(mongoTemplate, maxLockTime);
    }

    public GenericTemplateEntityRepositoryImpl(MongoTemplate mongoTemplate, Mutexes<Object> mutexes) {
        super(mongoTemplate, mutexes);
    }

}
