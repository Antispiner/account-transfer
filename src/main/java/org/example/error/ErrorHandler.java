package org.example.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    public void logError(String message, Throwable e) {
        logger.error(message, e);
    }
}
