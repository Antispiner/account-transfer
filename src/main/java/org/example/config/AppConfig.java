package org.example.config;

import jakarta.ws.rs.ApplicationPath;
import org.example.account.Account;
import org.example.error.ErrorHandler;
import org.example.manager.AccountManager;
import org.example.validator.TransferValidator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@ApplicationPath("/")
public class AppConfig extends ResourceConfig {
    public AppConfig() {
        packages("org.example.controller");
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                ErrorHandler errorHandler = new ErrorHandler();
                List<Account> initialAccounts = Arrays.asList(
                        new Account("123", BigDecimal.valueOf(1000)),
                        new Account("456", BigDecimal.valueOf(500)),
                        new Account("789", BigDecimal.valueOf(750))
                );
                AccountManager accountManager = new AccountManager(2, initialAccounts, errorHandler);
                TransferValidator transferValidator = new TransferValidator(accountManager);

                bind(accountManager).to(AccountManager.class);
                bind(transferValidator).to(TransferValidator.class);
            }
        });
    }
}
