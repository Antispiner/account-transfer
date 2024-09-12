import org.example.account.Account;
import org.example.error.ErrorHandler;
import org.example.manager.AccountManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountManagerTest {

    private AccountManager accountManager;
    private ErrorHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ErrorHandler();
        List<Account> initialAccounts = List.of(
                new Account("123", BigDecimal.valueOf(1000)),
                new Account("456", BigDecimal.valueOf(500))
        );
        accountManager = new AccountManager(2, initialAccounts, handler);
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
    void testIdempotencyKey() {
        String idempotencyKey = "txn-1";
        boolean success1 = accountManager.transferMoney(idempotencyKey, "123", "456", BigDecimal.valueOf(100));
        boolean success2 = accountManager.transferMoney(idempotencyKey, "123", "456", BigDecimal.valueOf(100));

        assertTrue(success1);
        assertFalse(success2);
        assertEquals(1, handler.getErrorLog().size());
    }

    @Test
    void testTransferMoneyInsufficientFunds() {
        String idempotencyKey = "txn-1";
        boolean success = accountManager.transferMoney(idempotencyKey,"456", "123", BigDecimal.valueOf(1000));
        assertFalse(success);
    }
}
