package com.mehmetozanguven.inghubs_digital_wallet.core;


import com.mehmetozanguven.inghubs_digital_wallet.core.clearDatabase.ClearDatabaseBeforeEach;
import com.mehmetozanguven.inghubs_digital_wallet.core.testcontainer.EnableTestcontainers;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ClearDatabaseBeforeEach
@EnableTestcontainers
@ActiveProfiles("test-containers")
public abstract class BaseDataJPATestcontainerTest {
}
