import org.example.account.Account;
import org.example.manager.AccountManager;
import org.example.model.TransferRequest;
import org.example.validator.TransferValidator;
import org.example.validator.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TransferValidatorTest {

    private TransferValidator validator;
    private AccountManager accountManager;

    @BeforeEach
    void setUp() {
        accountManager = new AccountManager(2, Arrays.asList(
                new Account("123", BigDecimal.valueOf(1000)),
                new Account("456", BigDecimal.valueOf(500))
        ), null);
        validator = new TransferValidator(accountManager);
    }

    @Test
    void testValidateSuccess() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId("123");
        request.setToAccountId("456");
        request.setAmount(BigDecimal.valueOf(100));

        assertDoesNotThrow(() -> validator.validate("key1", request));
    }

    @Test
    void testValidateNegativeAmount() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId("123");
        request.setToAccountId("456");
        request.setAmount(BigDecimal.valueOf(-100));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                validator.validate("key2", request));
        assertEquals("Transfer amount must be positive", exception.getMessage());
    }

    @Test
    void testValidateSameAccount() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId("123");
        request.setToAccountId("123");
        request.setAmount(BigDecimal.valueOf(100));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                validator.validate("key3", request));
        assertEquals("Cannot transfer money to the same account", exception.getMessage());
    }

    @Test
    void testValidateNonExistingFromAccount() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId("999");
        request.setToAccountId("456");
        request.setAmount(BigDecimal.valueOf(100));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                validator.validate("key4", request));
        assertEquals("From account does not exist", exception.getMessage());
    }

    @Test
    void testValidateNonExistingToAccount() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId("123");
        request.setToAccountId("999");
        request.setAmount(BigDecimal.valueOf(100));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                validator.validate("key5", request));
        assertEquals("To account does not exist", exception.getMessage());
    }

    @Test
    void testValidateNullIdempotencyKey() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId("123");
        request.setToAccountId("456");
        request.setAmount(BigDecimal.valueOf(100));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                validator.validate(null, request));
        assertEquals("Idempotency-Key header is missing or empty", exception.getMessage());
    }

    @Test
    void testValidateEmptyIdempotencyKey() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId("123");
        request.setToAccountId("456");
        request.setAmount(BigDecimal.valueOf(100));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                validator.validate("", request));
        assertEquals("Idempotency-Key header is missing or empty", exception.getMessage());
    }

    @Test
    void testValidateNullAmount() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId("123");
        request.setToAccountId("456");
        request.setAmount(null);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                validator.validate("key6", request));
        assertEquals("Transfer amount must be positive", exception.getMessage());
    }
}
