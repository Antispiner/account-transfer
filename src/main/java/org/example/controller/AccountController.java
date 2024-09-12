package org.example.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.example.model.dto.AccountDTO;
import org.example.manager.AccountManager;
import org.example.model.TransferRequest;
import org.example.validator.TransferValidator;
import org.example.validator.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Path("/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountManager accountManager;
    private final TransferValidator transferValidator;

    @Inject
    public AccountController(AccountManager accountManager, TransferValidator transferValidator) {
        this.accountManager = accountManager;
        this.transferValidator = transferValidator;
    }

    @GET
    @Produces("application/json")
    public Response getAllAccounts() {
        List<AccountDTO> accountList = accountManager.getAllAccounts().stream()
                .map(account -> new AccountDTO(account.getId(), account.getBalance()))
                .collect(Collectors.toList());
        return Response.ok(accountList).build();
    }

    @POST
    @Path("/transfer")
    @Consumes("application/json")
    @Produces("application/json")
    public Response transferMoney(@HeaderParam("Idempotency-Key") String idempotencyKey, TransferRequest request) {
        try {
            logger.info("Received request: from {}, to {}, amount {}",
                    request.getFromAccountId(), request.getToAccountId(), request.getAmount());
            transferValidator.validate(request);
            boolean success = accountManager.transferMoney(idempotencyKey, request.getFromAccountId(), request.getToAccountId(), request.getAmount());

            if (success) {
                return Response.ok("Transaction Successful").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Transaction Failed").build();
            }
        } catch (ValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage()).build();
        }
    }
}
