package com.vadim.ok.logging;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggerHelper {
    private static final ConsoleHandler threadConsoleHandler = new ConsoleHandler();
    private static final String LOGGING_PROPERTIES_FILE_NAME = "/logging.properties";


    static {
        threadConsoleHandler.setFormatter(new ThreadFormatter());
        threadConsoleHandler.setLevel(Level.ALL);

        try {
            InputStream loggingConfiguration = LoggerHelper.class.getResourceAsStream(LOGGING_PROPERTIES_FILE_NAME);
            LogManager.getLogManager().readConfiguration(loggingConfiguration);
        } catch (Exception e) {
            try {
                LogManager.getLogManager().readConfiguration();
                getLogger(LoggerHelper.class).severe(LOGGING_PROPERTIES_FILE_NAME + " was not found in classpath. Default logging config will be used.");
            } catch (IOException e1) {
                System.err.println("Can't load default logging setting. Logging will be disabled");
            }
        }
    }

    public static Logger getLogger(Class clazz) {
        return Logger.getLogger(clazz.getName());
    }

    public static Logger getThreadLogger(Class clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.setUseParentHandlers(false);
        logger.addHandler(threadConsoleHandler);

        return logger;
    }
}