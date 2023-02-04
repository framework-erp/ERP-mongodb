import erp.ERP;
import erp.mongodb.interfaceimplementer.InterfaceMongodbRepositoryImplementer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestRepositoryTest {

    @Test
    public void test() {
        TestEntityRepository<TestEntity, Object> testEntityRepository =
                InterfaceMongodbRepositoryImplementer.instance(TestEntityRepository.class, null);
        TestEntity testEntity0 = ERP.go("test", () -> {
            TestEntity testEntity = new TestEntityImpl("1");
            testEntityRepository.put(testEntity);
            return testEntity;
        });
        TestEntity testEntity1 = testEntityRepository.find(testEntity0.getId());
        assertEquals(testEntity1.getId(), testEntity0.getId());

        TestEntityImplRepository testEntityImplRepository =
                InterfaceMongodbRepositoryImplementer.instance(TestEntityImplRepository.class, null);
        TestEntityImpl testEntityImpl0 = ERP.go("test", () -> {
            TestEntityImpl testEntityImpl = new TestEntityImpl("1");
            testEntityImplRepository.put(testEntityImpl);
            return testEntityImpl;
        });
        TestEntityImpl testEntityImpl1 = testEntityImplRepository.find(testEntityImpl0.getId());
        assertEquals(testEntityImpl1.getId(), testEntityImpl0.getId());

    }

}
