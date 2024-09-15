# Banking Application

This service allows for safe and reliable money transfers between bank accounts. It supports handling multiple concurrent transactions, ensuring data integrity through proper synchronization, transaction management, and rollback mechanisms.

## Table of Contents

- [Features](#features)
- [How to Run the Service](#how-to-run-the-service)
- [API Usage](#api-usage)

### Features

1. **Concurrent Transactions: Supports simultaneous money transfers without data corruption.**
2. **Thread Safety: Employs synchronization techniques to prevent race conditions.**
3. **Atomic Transactions: Ensures each transaction is either fully completed or has no effect.**
4. **Deferred Commit: Utilizes provisional balances to validate and commit changes atomically.**
5. **Rollback Mechanism: Implements rollback functionality to revert changes if a transaction fails.**
6. **Idempotency: Uses idempotency keys to prevent duplicate transactions.**
7. **Deadlock Prevention: Acquires locks in a consistent order to avoid deadlocks.**
8. **Error Handling and Logging: Comprehensive error handling with SLF4J logging for monitoring.**


### How to Run the Service

1. **Prerequisites**:
    - Java 17 or higher installed on your system.
    - Gradle installed for managing dependencies and building the project.

2. **Building the Project**:
    - Use Gradle to build the project:
      ```bash
      gradle build
      ```
   -  Run as a standalone application (Main.class).

### API Usage

- **URL**: `http://localhost:8090/accounts`
- **Method**: `GET`
- **Body**: empty

**Response**:
```csv
[
    {
        "accountId": "123",
        "balance": 400.00
    },
    {
        "accountId": "789",
        "balance": 1350.00
    },
    {
        "accountId": "456",
        "balance": 500.00
    }
]
```

- **URL**: `http://localhost:8090/accounts/transfer`
- **Method**: `POST`
- **Content-Type**: `application/json`
- **Idempotency-Key**: `<unique-key>`
- **Body**: `
  {
  "fromAccountId": "123",
  "toAccountId": "456",
  "amount": 100.00
  }
  `
**Response**:
```csv
Success (200 OK):
{
  "message": "Transaction Successful"
}

Failure (400 Bad Request):
{
  "message": "Transaction Failed",
  "reason": "Insufficient funds"
}
```


