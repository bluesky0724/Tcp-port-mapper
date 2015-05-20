package com.vadim.ok.portmapper.tasks;


public class TaskExecutionStatistics {
    private long transmittedBytesFromClientToTargetCount;
    private long transmittedBytesFromTargetToClientCount;
    private int byteBufferCapacity;
    private String threadName;

    public TaskExecutionStatistics(String threadName) {
        this.threadName = threadName;
    }

    public long getTransmittedBytesFromClientToTargetCount() {
        return transmittedBytesFromClientToTargetCount;
    }

    void setTransmittedBytesFromClientToTargetCount(long transmittedBytesFromClientToTargetCount) {
        this.transmittedBytesFromClientToTargetCount = transmittedBytesFromClientToTargetCount;
    }

    public long getTransmittedBytesFromTargetToClientCount() {
        return transmittedBytesFromTargetToClientCount;
    }

    void setTransmittedBytesFromTargetToClientCount(long transmittedBytesFromTargetToClientCount) {
        this.transmittedBytesFromTargetToClientCount = transmittedBytesFromTargetToClientCount;
    }

    public int getByteBufferCapacity() {
        return byteBufferCapacity;
    }

    void setByteBufferCapacity(int byteBufferCapacity) {
        this.byteBufferCapacity = byteBufferCapacity;
    }

    public String getThreadName() {
        return threadName;
    }
}
