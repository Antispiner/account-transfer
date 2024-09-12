package org.example.validator;

import org.example.account.Account;
import org.example.manager.AccountManager;
import org.example.model.TransferRequest;

import java.math.BigDecimal;

public class TransferValidator {
    private final AccountManager accountManager;

    public TransferValidator(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public void validate(TransferRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Transfer amount must be positive");
        }

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new ValidationException("Cannot transfer money to the same account");
        }

        Account fromAccount = accountManager.getSegmentByAccountId(request.getFromAccountId()).getAccount(request.getFromAccountId());
        Account toAccount = accountManager.getSegmentByAccountId(request.getToAccountId()).getAccount(request.getToAccountId());

        if (fromAccount == null) {
            throw new ValidationException("From account does not exist");
        }

        if (toAccount == null) {
            throw new ValidationException("To account does not exist");
        }
    }
}
