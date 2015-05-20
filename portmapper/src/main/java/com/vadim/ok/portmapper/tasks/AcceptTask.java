package com.vadim.ok.portmapper.tasks;

import com.vadim.ok.logging.LoggerHelper;
import com.vadim.ok.portmapper.config.ServiceDescriptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class AcceptTask implements Callable<TaskExecutionStatistics> {
    private static final Logger logger = LoggerHelper.getThreadLogger(AcceptTask.class);

    private final SocketChannel clientSocketChannel;
    private final ServiceDescriptor serviceDescriptor;
    private final TaskExecutionStatistics taskExecutionStatistics = new TaskExecutionStatistics(Thread.currentThread().getName());

    public AcceptTask(SocketChannel clientSocketChannel, ServiceDescriptor serviceDescriptor, int byteBufferCapacity) {
        this.clientSocketChannel = clientSocketChannel;
        this.serviceDescriptor = serviceDescriptor;
        taskExecutionStatistics.setByteBufferCapacity(byteBufferCapacity);
    }

    private ByteBuffer reallocate(ByteBuffer oldByteBuffer) {
        taskExecutionStatistics.setByteBufferCapacity(taskExecutionStatistics.getByteBufferCapacity() * 2);

        ByteBuffer newByteBuffer = ByteBuffer.allocate(taskExecutionStatistics.getByteBufferCapacity());

        for (int i = 0; i < oldByteBuffer.position(); ++i) {
            newByteBuffer.put(oldByteBuffer.get(i));
        }

        return newByteBuffer;
    }

    @Override
    public TaskExecutionStatistics call() throws Exception {
        logger.info("start");

        SocketChannel targetSocketChannel = SocketChannel.open();
        targetSocketChannel.connect(new InetSocketAddress(serviceDescriptor.getRemoteHost(), serviceDescriptor.getRemotePort()));
        targetSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        targetSocketChannel.register(selector, SelectionKey.OP_READ, clientSocketChannel);
        clientSocketChannel.register(selector, SelectionKey.OP_READ, targetSocketChannel);

        ByteBuffer byteBuffer = ByteBuffer.allocate(taskExecutionStatistics.getByteBufferCapacity());

        while (selector.keys().size() > 0) {
            if (selector.select() == 0) {
                continue;
            }

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();

                SocketChannel from = ((SocketChannel) selectionKey.channel());
                SocketChannel to = ((SocketChannel) selectionKey.attachment());
                logger.finest("isReadable: from " + from.getRemoteAddress() + " to " + to.getRemoteAddress());

                int bytesRead;
                boolean needReallocate;

                // Assumptions:
                // 1) byteBuffer is large enough to read all data at once. Else it will be reallocated
                // 2) bytesRead = 0 only if byteBuffer in not large enough. But according to
                //   the (1) assumption it will never happen.
                byteBuffer.clear();
                do {
                    try {
                        bytesRead = from.read(byteBuffer);
                    } catch (IOException e) {
                        bytesRead = -1;
                        logger.info("Can't read from a closed socket: " + e.getMessage());
                    }

                    if (bytesRead == -1) {
                        logger.finest("closed");
                        break;
                    }

                    needReallocate = byteBuffer.remaining() == 0;

                    if (needReallocate) {
                        logger.warning("REALLOCATE! Value " + taskExecutionStatistics.getByteBufferCapacity() + " bytes is not enough for this application.");
                        byteBuffer = reallocate(byteBuffer);
                    }
                } while (needReallocate);

                if (byteBuffer.position() > 0) {
                    try {
                        int bytesWritten;
                        if (serviceDescriptor.isHttpBasedProtocol() && isFromClientToTargetTransmission(from)) {
                            bytesWritten = to.write(HttpHeadersMapper.mapHttpHeaders(byteBuffer, serviceDescriptor));
                        } else {
                            byteBuffer.flip();
                            bytesWritten = to.write(byteBuffer);
                        }

                        collectTaskStatistics(bytesWritten, from);
                    } catch (IOException e) {
                        logger.info("Can't write to a closed socket: " + e.getMessage());
                    }
                }

                if (bytesRead == -1) {
                    from.shutdownInput();
                    to.shutdownOutput();
                    selectionKey.cancel();

                    // cancel operation takes effect only on the next select(), so force the next select
                    selector.wakeup();
                }

                iterator.remove();
            }
        }

        selector.close();
        clientSocketChannel.close();
        targetSocketChannel.close();

        logger.info("finish");

        return taskExecutionStatistics;

    }

    private void collectTaskStatistics(int transmittedBytes, SocketChannel from) {
        if (isFromClientToTargetTransmission(from)) {
            long total = taskExecutionStatistics.getTransmittedBytesFromClientToTargetCount();
            taskExecutionStatistics.setTransmittedBytesFromClientToTargetCount(total + transmittedBytes);
        } else {
            long total = taskExecutionStatistics.getTransmittedBytesFromTargetToClientCount();
            taskExecutionStatistics.setTransmittedBytesFromTargetToClientCount(total + transmittedBytes);
        }
    }

    private boolean isFromClientToTargetTransmission(SocketChannel from) {
        return from == clientSocketChannel;
    }
}
