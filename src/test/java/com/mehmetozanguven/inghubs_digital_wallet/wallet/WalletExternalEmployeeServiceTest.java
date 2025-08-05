package com.mehmetozanguven.inghubs_digital_wallet.wallet;

import com.mehmetozanguven.inghubs_digital_wallet.core.BaseApplicationModuleTest;
import com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.TransactionKafkaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@ApplicationModuleTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, extraIncludes = "core")
class WalletExternalEmployeeServiceTest extends BaseApplicationModuleTest {
    @Autowired
    WalletExternalEmployeeService walletExternalEmployeeService;
    @MockitoSpyBean
    TransactionKafkaService transactionKafkaService;

    @Test
    void approveTransaction_ShouldNotCallKafkaService_WhenTransactionNotFound() {
        walletExternalEmployeeService.approveTransaction("test", "test");
        Mockito.verifyNoInteractions(transactionKafkaService);
    }
}