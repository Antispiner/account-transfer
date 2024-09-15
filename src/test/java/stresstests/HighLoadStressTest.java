package stresstests;

import org.example.account.Account;
import org.example.error.ErrorHandler;
import org.example.manager.AccountManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HighLoadStressTest {

    private AccountManager accountManager;
    private ErrorHandler errorHandler;
    private List<Account> accounts;
    private static final int NUM_ACCOUNTS = 100;
    private static final int NUM_THREADS = 50;
    private static final int NUM_TRANSFERS_PER_THREAD = 5000;

    @BeforeEach
    void setUp() {
        errorHandler = new ErrorHandler();
        accounts = new ArrayList<>();
        for (int i = 0; i < NUM_ACCOUNTS; i++) {
            accounts.add(new Account(String.valueOf(i), BigDecimal.valueOf(100000)));
        }
        accountManager = new AccountManager(10, accounts, errorHandler);
    }

    @Test
    void testHighLoadTransfersWithRollback() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        AtomicInteger idGenerator = new AtomicInteger(0);

        for (int i = 0; i < NUM_THREADS; i++) {
            executorService.submit(() -> {
                Random random = new Random();
                for (int j = 0; j < NUM_TRANSFERS_PER_THREAD; j++) {
                    int fromIndex = random.nextInt(NUM_ACCOUNTS);
                    int toIndex;
                    do {
                        toIndex = random.nextInt(NUM_ACCOUNTS);
                    } while (toIndex == fromIndex);

                    String fromAccountId = String.valueOf(fromIndex);
                    String toAccountId = String.valueOf(toIndex);

                    boolean shouldFail = random.nextBoolean();
                    BigDecimal amount;
                    if (shouldFail) {
                        amount = BigDecimal.valueOf(200000 + random.nextInt(100000));
                    } else {
                        amount = BigDecimal.valueOf(random.nextInt(1000));
                    }

                    String idempotencyKey = UUID.randomUUID() + "-" + idGenerator.incrementAndGet();

                    accountManager.transferMoney(idempotencyKey, fromAccountId, toAccountId, amount);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.MINUTES);

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(BigDecimal.valueOf(NUM_ACCOUNTS * 100000), totalBalance);
    }
}
