package com.vadim.ok.portmapper.statistics;

import com.vadim.ok.logging.LoggerHelper;
import com.vadim.ok.portmapper.tasks.TaskExecutionStatistics;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

class TaskExecutionStatisticsAggregator implements Callable<Integer> {
    private static final Logger logger = LoggerHelper.getLogger(TaskExecutionStatisticsAggregator.class);

    private TotalExecutionStatistics totalExecutionStatistics;

    public TaskExecutionStatisticsAggregator(TotalExecutionStatistics totalExecutionStatistics) {
        this.totalExecutionStatistics = totalExecutionStatistics;
    }

    @Override
    public Integer call() throws Exception {
        logger.info("start");

        int freeCellsCount = 0;

        for (int i = 0; i < totalExecutionStatistics.taskExecutionStatisticsHolderBuffer.length; ++i) {
            Future<TaskExecutionStatistics> taskExecutionStatistics = totalExecutionStatistics.taskExecutionStatisticsHolderBuffer[i];

            if (taskExecutionStatistics.isDone()) {
                try {
                    totalExecutionStatistics.totalTransmittedBytesFromClientToTargetCount +=
                            taskExecutionStatistics.get().getTransmittedBytesFromClientToTargetCount();
                    totalExecutionStatistics.totalTransmittedBytesFromTargetToClientCount +=
                            taskExecutionStatistics.get().getTransmittedBytesFromTargetToClientCount();

                    totalExecutionStatistics.byteBufferCapacity = taskExecutionStatistics.get().getByteBufferCapacity();
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Accept task was interrupted during execution", e);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    logger.log(Level.WARNING, "Accept task failed", cause);
                } finally {
                    totalExecutionStatistics.taskExecutionStatisticsHolderBuffer[i] = null;
                    freeCellsCount++;
                }
            }
        }

        logger.info("finish");

        return freeCellsCount;
    }
}