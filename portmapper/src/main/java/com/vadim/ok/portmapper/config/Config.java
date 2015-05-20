package com.vadim.ok.portmapper.config;

import com.vadim.ok.logging.LoggerHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class Config extends Properties {
    private static final Logger logger = LoggerHelper.getLogger(Config.class);

    private static final String PROPERTIES_FILE_NAME = "/proxy.properties";
    private static final String USE_DEFAULT_VALUE_MESSAGE = "Property %s is missing in " + PROPERTIES_FILE_NAME + ". Used default value \"%s\" instead.";

    private static final String LOCAL_PORT_KEY = ".localPort";
    private static final String REMOTE_HOST_KEY = ".remoteHost";
    private static final String REMOTE_PORT_KEY = ".remotePort";
    private static final String HTTP_BASED_PROTOCOL_SERVICE_KEY = "web";


    private static final String THREAD_POOL_CAPACITY_KEY = "threadPoolCapacity";
    private static final int THREAD_POOL_CAPACITY_DEFAULT_VALUE = 10;


    private static final String INITIAL_BYTE_BUFFER_CAPACITY_KEY = "initialByteBufferCapacity";
    // The strategy is to place all transmitted data in the buffer at once.
    // The most performance can be achieved if all data can be written in the buffer at once.
    // So we should keep this value pretty large.
    // If byte buffer is not enough to store all transmitted data the system will reallocate the buffer with more
    // capacity.
    private static final int INITIAL_BYTE_BUFFER_CAPACITY_DEFAULT_VALUE = 3145728; // 3 Megabytes

    private static final String TOTAL_EXECUTION_STATISTICS_BUFFER_CAPACITY_KEY = "totalExecutionStatisticsBufferCapacity";
    private static final int TOTAL_EXECUTION_STATISTICS_BUFFER_CAPACITY_DEFAULT_VALUE = 10;

    private static final String WEB_ADMIN_CONSOLE_PORT_KEY = "webAdminConsolePort";
    private static final int WEB_ADMIN_CONSOLE_PORT_DEFAULT_VALUE = 9999;


    private static final Config instance = new Config();

    private Set<ServiceDescriptor> serviceDescriptors;

    private Config() {
        try {
            load(Config.class.getResourceAsStream(PROPERTIES_FILE_NAME));

            serviceDescriptors = new HashSet<>();

            // Assume that properties might not be in order
            for (String property : stringPropertyNames()) {
                if (property.contains(LOCAL_PORT_KEY)) {
                    serviceDescriptors.add(new ServiceDescriptor(property.replace(LOCAL_PORT_KEY, "")));
                }
            }

            for (ServiceDescriptor serviceDescriptor : serviceDescriptors) {
                String propertyName = serviceDescriptor.getServiceId() + LOCAL_PORT_KEY;
                String property = getProperty(propertyName);
                if (property == null) {
                    throw new InvalidPropertiesFormatException("Can't find property " + propertyName);
                }
                serviceDescriptor.setLocalPort(Integer.valueOf(property));


                propertyName = serviceDescriptor.getServiceId() + REMOTE_HOST_KEY;
                property = getProperty(propertyName);
                if (property == null) {
                    throw new InvalidPropertiesFormatException("Can't find property " + propertyName);
                }
                serviceDescriptor.setRemoteHost(property);


                propertyName = serviceDescriptor.getServiceId() + REMOTE_PORT_KEY;
                property = getProperty(propertyName);
                if (property == null) {
                    throw new InvalidPropertiesFormatException("Can't find property " + propertyName);
                }
                serviceDescriptor.setRemotePort(Integer.valueOf(property));

                if (serviceDescriptor.getServiceId().equals(HTTP_BASED_PROTOCOL_SERVICE_KEY)) {
                    serviceDescriptor.setHttpBasedProtocol(true);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cant' load properties file", e);
        }
    }

    public static Config getInstance() {
        return instance;
    }

    public Set<ServiceDescriptor> getServiceDescriptors() {
        return serviceDescriptors;
    }

    public int getInitialByteBufferCapacity() {
        return getPropertyOrDefaultWithNotification(INITIAL_BYTE_BUFFER_CAPACITY_KEY,
                INITIAL_BYTE_BUFFER_CAPACITY_DEFAULT_VALUE);
    }

    public int getThreadPoolCapacity() {
        return getPropertyOrDefaultWithNotification(THREAD_POOL_CAPACITY_KEY, THREAD_POOL_CAPACITY_DEFAULT_VALUE);
    }

    public int getTotalExecutionStatisticsBufferCapacity() {
        return getPropertyOrDefaultWithNotification(TOTAL_EXECUTION_STATISTICS_BUFFER_CAPACITY_KEY,
                TOTAL_EXECUTION_STATISTICS_BUFFER_CAPACITY_DEFAULT_VALUE);
    }

    public int getWebAdminConsolePort() {
        return getPropertyOrDefaultWithNotification(WEB_ADMIN_CONSOLE_PORT_KEY,
                WEB_ADMIN_CONSOLE_PORT_DEFAULT_VALUE);
    }

    private int getPropertyOrDefaultWithNotification(String key, int defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            logger.info(String.format(USE_DEFAULT_VALUE_MESSAGE, key, defaultValue));
            return defaultValue;
        }

        return Integer.valueOf(value);
    }
}
