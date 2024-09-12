package org.example.transaction;

import org.example.account.Account;

import java.math.BigDecimal;

public record TransactionEntry(Account account, BigDecimal amount) {
}
