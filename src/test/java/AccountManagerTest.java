import org.example.account.Account;
import org.example.error.ErrorHandler;
import org.example.manager.AccountManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AccountManagerTest {

    private AccountManager accountManager;
    private ErrorHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ErrorHandler();
        accountManager = new AccountManager(2, Arrays.asList(
                new Account("123", BigDecimal.valueOf(1000)),
                new Account("456", BigDecimal.valueOf(500))),
                handler);
    }

    @Test
    void testGetAllAccounts() {
        assertEquals(2, accountManager.getAllAccounts().size());
    }

    @Test
    void testTransferMoneySuccess() {
        String idempotencyKey = "txn-1";
        boolean success = accountManager.transferMoney(idempotencyKey, "123", "456", BigDecimal.valueOf(100));
        assertTrue(success);
        assertEquals(0, accountManager.getSegmentByAccountId("123").getAccount("123").getBalance().compareTo(BigDecimal.valueOf(900)));
        assertEquals(0, accountManager.getSegmentByAccountId("456").getAccount("456").getBalance().compareTo(BigDecimal.valueOf(600)));

    }

    @Test
    void testTransferMoneyInsufficientFunds() {
        String idempotencyKey = "txn-1";
        boolean success = accountManager.transferMoney(idempotencyKey,"456", "123", BigDecimal.valueOf(1000));
        assertFalse(success);
    }

    @Test
    void testTransferMoneyDuplicateIdempotencyKey() {
        boolean firstAttempt = accountManager.transferMoney("key3", "123", "456", BigDecimal.valueOf(100));
        boolean secondAttempt = accountManager.transferMoney("key3", "123", "456", BigDecimal.valueOf(100));

        assertTrue(firstAttempt);
        assertFalse(secondAttempt);
        assertEquals(BigDecimal.valueOf(900), accountManager.getAccount("123").getBalance());
        assertEquals(BigDecimal.valueOf(600), accountManager.getAccount("456").getBalance());
    }

    @Test
    void testTransferMoneyInvalidAccounts() {
        boolean result = accountManager.transferMoney("key4", "999", "456", BigDecimal.valueOf(100));
        assertFalse(result);
    }

    @Test
    void testTransferMoneyNullIdempotencyKey() {
        boolean result = accountManager.transferMoney(null, "123", "456", BigDecimal.valueOf(100));
        assertFalse(result);
    }

    @Test
    void testTransferMoneyEmptyIdempotencyKey() {
        boolean result = accountManager.transferMoney("", "123", "456", BigDecimal.valueOf(100));
        assertFalse(result);
    }

    @Test
    void testTransactionRollbackOnFailure() {
        boolean result = accountManager.transferMoney("key5", "456", "123", BigDecimal.valueOf(1000));
        assertFalse(result);

        assertEquals(BigDecimal.valueOf(1000), accountManager.getAccount("123").getBalance());
        assertEquals(BigDecimal.valueOf(500), accountManager.getAccount("456").getBalance());
    }

    @Test
    void testConcurrentTransfersWithRollback() {
        boolean result1 = accountManager.transferMoney("key6", "123", "456", BigDecimal.valueOf(100));
        boolean result2 = accountManager.transferMoney("key7", "123", "456", BigDecimal.valueOf(2000)); // Should fail
        boolean result3 = accountManager.transferMoney("key8", "456", "123", BigDecimal.valueOf(100));

        assertTrue(result1);
        assertFalse(result2);
        assertTrue(result3);

        assertEquals(BigDecimal.valueOf(1000 - 100 + 100), accountManager.getAccount("123").getBalance()); // 1000 - 100 + 100 = 1000
        assertEquals(BigDecimal.valueOf(500 + 100 - 100), accountManager.getAccount("456").getBalance()); // 500 + 100 - 100 = 500
    }
}
