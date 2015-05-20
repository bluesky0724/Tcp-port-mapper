package com.vadim.ok.portmapper;

import com.vadim.ok.logging.LoggerHelper;
import com.vadim.ok.portmapper.config.Config;
import com.vadim.ok.portmapper.config.ServiceDescriptor;
import com.vadim.ok.portmapper.statistics.TotalExecutionStatistics;
import com.vadim.ok.portmapper.tasks.AcceptTask;
import com.vadim.ok.portmapper.tasks.TaskExecutionStatistics;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpPortMapper extends Thread {
    private static final Logger logger = LoggerHelper.getLogger(TcpPortMapper.class);

    private final ExecutorService executorService;
    private final TotalExecutionStatistics totalExecutionStatistics;

    public TcpPortMapper(ExecutorService executorService, TotalExecutionStatistics totalExecutionStatistics) {
        this.executorService = executorService;
        this.totalExecutionStatistics = totalExecutionStatistics;
    }

    @Override
    public void run() {
        Set<ServiceDescriptor> serviceDescriptors = Config.getInstance().getServiceDescriptors();

        Selector selector = null;

        try {
            selector = Selector.open();

            for (ServiceDescriptor serviceDescriptor : serviceDescriptors) {
                ServerSocketChannel proxyServerSocketChannel = ServerSocketChannel.open();
                proxyServerSocketChannel.configureBlocking(false);
                proxyServerSocketChannel.socket().bind(new InetSocketAddress(serviceDescriptor.getLocalPort()));
                proxyServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT, serviceDescriptor);
                logger.info("Service \"" + serviceDescriptor.getServiceId() + "\" started listening on port " +
                        serviceDescriptor.getLocalPort() + " (remote host: " + serviceDescriptor.getRemoteHost() + ", remote port: " + serviceDescriptor.getRemotePort() + ")");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't establish connection. The system will exit.", e);
            System.exit(-1);
        }


        try {
            while (selector.select() != 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel clientSocketChannel = serverSocketChannel.accept();
                    clientSocketChannel.configureBlocking(false);
                    ServiceDescriptor serviceDescriptor = (ServiceDescriptor) selectionKey.attachment();
                    logger.info("Accepted connection on address " + serverSocketChannel.getLocalAddress() + " from " + clientSocketChannel.getRemoteAddress());
                    Future<TaskExecutionStatistics> taskExecutionStatistics =
                            executorService.submit(new AcceptTask(clientSocketChannel, serviceDescriptor, totalExecutionStatistics.getByteBufferCapacity()));
                    totalExecutionStatistics.put(taskExecutionStatistics);
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't accept connection. The system will exit.", e);
            System.exit(-1);
        }
    }
}
