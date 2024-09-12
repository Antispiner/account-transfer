package org.example.error;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private final List<String> errorLog = new ArrayList<>();

    public void logError(String message, Throwable e) {
        String logEntry = String.format("Error: %s - Exception: %s", message, e.getMessage());
        errorLog.add(logEntry);
        System.out.println(logEntry);
    }

    public List<String> getErrorLog() {
        return errorLog;
    }
}
