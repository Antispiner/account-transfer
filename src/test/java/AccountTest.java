import org.example.account.Account;
import org.example.transaction.TransactionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private Account account;
    private TransactionContext context;

    @BeforeEach
    void setUp() {
        account = new Account("123", BigDecimal.valueOf(1000));
        context = new TransactionContext(1);
    }

    @Test
    void testProvisionalUpdateSuccess() {
        boolean result = account.provisionalUpdate(BigDecimal.valueOf(-500), context);
        assertTrue(result);
        assertEquals(BigDecimal.valueOf(500), context.getProvisionalBalance(account));
        assertEquals(BigDecimal.valueOf(1000), context.getOriginalBalance(account));
    }

    @Test
    void testCommitProvisionalBalance() {
        account.provisionalUpdate(BigDecimal.valueOf(-200), context);
        account.commitProvisionalBalance(context);
        assertEquals(BigDecimal.valueOf(800), account.getBalance());
        assertNull(context.getProvisionalBalance(account));
        assertNull(context.getOriginalBalance(account));
    }

    @Test
    void testRollbackToOriginalBalance() {
        account.provisionalUpdate(BigDecimal.valueOf(-200), context);
        account.rollbackToOriginalBalance(context);
        assertEquals(BigDecimal.valueOf(1000), account.getBalance());
        assertNull(context.getProvisionalBalance(account));
        assertNull(context.getOriginalBalance(account));
    }

    @Test
    void testMultipleProvisionalUpdatesAndRollback() {
        account.provisionalUpdate(BigDecimal.valueOf(-200), context);
        account.provisionalUpdate(BigDecimal.valueOf(-100), context);
        BigDecimal provisionalBalance = context.getProvisionalBalance(account);
        assertEquals(BigDecimal.valueOf(700), provisionalBalance);

        account.rollbackToOriginalBalance(context);
        assertEquals(BigDecimal.valueOf(1000), account.getBalance());
        assertNull(context.getProvisionalBalance(account));
        assertNull(context.getOriginalBalance(account));
    }
}
