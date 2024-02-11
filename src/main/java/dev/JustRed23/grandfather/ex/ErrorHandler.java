package dev.JustRed23.grandfather.ex;

import dev.JustRed23.stonebrick.log.SBLogger;
import org.slf4j.Logger;

public final class ErrorHandler {

    private static final Logger LOGGER = SBLogger.getLogger(ErrorHandler.class);

    public static void handleException(String action, Throwable e) {
        LOGGER.error("An error occurred while executing action '" + action + "'!", e);
    }
}
