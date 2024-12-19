package erp.mongodb;

import erp.repository.SingletonEntity;
import erp.repository.SingletonRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;

public class MongodbSingletonRepository<E> extends SingletonRepository<E> {
    private static MongodbRepository<SingletonEntity, String> SINGLETON_CONTAINER;

    private static void init(MongoTemplate mongoTemplate) {
        if (SINGLETON_CONTAINER == null) {
            synchronized (MongodbSingletonRepository.class) {
                if (SINGLETON_CONTAINER == null) {
                    SINGLETON_CONTAINER = new MongodbRepository<>(mongoTemplate, SingletonEntity.class,
                            "erp.mongodb.MongodbSingletonRepository");
                }
            }
        }
    }

    protected MongodbSingletonRepository(MongoTemplate mongoTemplate) {
        init(mongoTemplate);
        this.singletonEntitiesContainer = SINGLETON_CONTAINER;
    }

    public MongodbSingletonRepository(MongoTemplate mongoTemplate, String repositoryName) {
        super(repositoryName);
        init(mongoTemplate);
        this.singletonEntitiesContainer = SINGLETON_CONTAINER;
    }

    public MongodbSingletonRepository(MongoTemplate mongoTemplate, E entity) {
        super(entity);
        init(mongoTemplate);
        this.singletonEntitiesContainer = SINGLETON_CONTAINER;
        ensureEntity(entity);
    }

    public MongodbSingletonRepository(MongoTemplate mongoTemplate, E entity, String repositoryName) {
        super(repositoryName);
        init(mongoTemplate);
        this.singletonEntitiesContainer = SINGLETON_CONTAINER;
        ensureEntity(entity);
    }

    private void ensureEntity(E entity) {
        SingletonEntity singletonEntity = new SingletonEntity();
        singletonEntity.setName(name);
        singletonEntity.setEntity(entity);
        try {
            this.singletonEntitiesContainer.getStore().insert(name, singletonEntity);
        } catch (DuplicateKeyException e) {
            //什么也不用做
        }
    }

}