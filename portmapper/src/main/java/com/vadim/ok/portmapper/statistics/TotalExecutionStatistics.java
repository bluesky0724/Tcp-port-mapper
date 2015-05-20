package com.vadim.ok.portmapper.statistics;

import com.vadim.ok.logging.LoggerHelper;
import com.vadim.ok.portmapper.tasks.TaskExecutionStatistics;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TotalExecutionStatistics {
    private static final Logger logger = LoggerHelper.getLogger(TotalExecutionStatistics.class);

    final Future<TaskExecutionStatistics>[] taskExecutionStatisticsHolderBuffer;
    private long freeCellsCount;

    long totalTransmittedBytesFromClientToTargetCount;
    long totalTransmittedBytesFromTargetToClientCount;
    int byteBufferCapacity;

    private Future<Integer> taskExecutionStatisticsAggregatorFuture;
    private final ExecutorService executorService;

    public TotalExecutionStatistics(final int capacity, int initialByteBufferCapacity, ExecutorService executorService) {
        taskExecutionStatisticsHolderBuffer = new Future[capacity];
        freeCellsCount = capacity;
        byteBufferCapacity = initialByteBufferCapacity;
        this.executorService = executorService;
    }

    public long getTotalTransmittedBytesFromClientToTargetCount() {
        return totalTransmittedBytesFromClientToTargetCount;
    }

    public long getTotalTransmittedBytesFromTargetToClientCount() {
        return totalTransmittedBytesFromTargetToClientCount;
    }

    public int getByteBufferCapacity() {
        return byteBufferCapacity;
    }

    public void put(Future<TaskExecutionStatistics> taskTaskExecutionStatistics) {
        if (taskExecutionStatisticsAggregatorFuture != null) {
            if (taskExecutionStatisticsAggregatorFuture.isDone()) {
                try {
                    freeCellsCount = taskExecutionStatisticsAggregatorFuture.get();
                    taskExecutionStatisticsAggregatorFuture = null;
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "interrupted", e);
                } catch (ExecutionException e) {
                    logger.log(Level.WARNING, "exception during execution", e.getCause());
                }

                if (freeCellsCount == 0) {
                    logger.info("Statistics aggregation is in process. Statistics of the currently finished thread will be lost");
                }
            } else {
                logger.info("Statistics aggregation is in process. Statistics of the currently finished thread will be lost");
                return;
            }
        }

        for (int i = 0; i < taskExecutionStatisticsHolderBuffer.length && freeCellsCount != 0; ++i) {
            if (taskExecutionStatisticsHolderBuffer[i] == null) {
                taskExecutionStatisticsHolderBuffer[i] = taskTaskExecutionStatistics;
                freeCellsCount--;
                break;
            }
        }

        if (freeCellsCount == 0) {
            taskExecutionStatisticsAggregatorFuture = executorService.submit(new TaskExecutionStatisticsAggregator(this));
        }
    }
}