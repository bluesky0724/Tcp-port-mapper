package com.vadim.ok.main;

import com.vadim.ok.logging.LoggerHelper;
import com.vadim.ok.portmapper.TcpPortMapper;
import com.vadim.ok.portmapper.config.Config;
import com.vadim.ok.portmapper.statistics.TotalExecutionStatistics;
import com.vadim.ok.webadminconsole.server.WebAdminConsole;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * There is no any synchronization between port mapper thread and traffic statistics (total bytes/write) aggregation
 * thread. It provides better performance but traffic statistics of some individual threads may not be taken into
 * consideration.
 */
public class Main {
    private static final Logger logger = LoggerHelper.getLogger(Main.class);

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(Config.getInstance().getThreadPoolCapacity());

        TotalExecutionStatistics totalExecutionStatistics =
                new TotalExecutionStatistics(Config.getInstance().getTotalExecutionStatisticsBufferCapacity(),
                        Config.getInstance().getInitialByteBufferCapacity(),
                        executorService);

        TcpPortMapper tcpPortMapper = new TcpPortMapper(executorService, totalExecutionStatistics);
        tcpPortMapper.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.log(Level.SEVERE, "Unexpected error. The system will exit.", e);
                System.exit(-1);
            }
        });
        tcpPortMapper.start();

        new WebAdminConsole(totalExecutionStatistics, Config.getInstance().getWebAdminConsolePort()).start();
    }
}
