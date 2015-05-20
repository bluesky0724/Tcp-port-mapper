package com.vadim.ok.webadminconsole.server;

import com.vadim.ok.logging.LoggerHelper;
import com.vadim.ok.portmapper.statistics.TotalExecutionStatistics;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebAdminConsole extends Thread {
    private static final Logger logger = LoggerHelper.getLogger(WebAdminConsole.class);

    private static final int BYTE_BUFFER_CAPACITY = 3145728; // 3 Megabytes
    private static final int FILE_CAPACITY = BYTE_BUFFER_CAPACITY;
    private static final int CR = 13;
    private static final int LF = 10;

    private static final String HTML_ROOT_PATH = "/site/";
    private static final String ERROR_400_FILENAME = HTML_ROOT_PATH + "errors/400.html";
    private static final String ERROR_404_FILENAME = HTML_ROOT_PATH + "errors/404.html";

    private static final String REST_REQUEST_KEY = "GET /rest/";
    private final RestService restService;
    private final int webAdminConsolePort;

    public WebAdminConsole(TotalExecutionStatistics totalExecutionStatistics, int webAdminConsolePort) {
        this.restService = new RestService(totalExecutionStatistics);
        this.webAdminConsolePort = webAdminConsolePort;
    }

    @Override
    public void run() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(webAdminConsolePort));
            logger.info("Web Admin Console started on port " + webAdminConsolePort);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (selector.select() != 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();

                    if (selectionKey.isAcceptable()) {
                        SocketChannel clientSocketChannel = ((ServerSocketChannel)selectionKey.channel()).accept();
                        clientSocketChannel.configureBlocking(false);
                        ByteBuffer byteBuffer = ByteBuffer.allocate(BYTE_BUFFER_CAPACITY);
                        clientSocketChannel.register(selector, SelectionKey.OP_READ, byteBuffer);
                        logger.finest("Accept from " + clientSocketChannel.getRemoteAddress());
                    } else if (selectionKey.isReadable()) {
                        SocketChannel clientSocketChannel = (SocketChannel)selectionKey.channel();
                        logger.finest("isReadable from " + clientSocketChannel.getRemoteAddress());
                        int bytesCount;
                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        try {
                            bytesCount = clientSocketChannel.read(byteBuffer);
                        } catch (IOException e) {
                            logger.finest("Peer cancelled connection: " + e.getMessage());
                            bytesCount = -1;
                        }

                        logger.finest("bytes read: " + bytesCount);

                        if (bytesCount == -1) {
                            clientSocketChannel.close();
                            selectionKey.cancel();
                            selectionKey.attach(null); // free byte buffer
                            logger.finest("client closed connection");
                        } else {
                            if (isHttpRequestReadyForProcessing(byteBuffer)) {
                                bytesCount = clientSocketChannel.write(fetchResponse(byteBuffer));
                                byteBuffer.clear();
                                logger.finest("bytes written: " + bytesCount);
                            }
                        }
                    }
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            // failing of this service is not critical
            logger.log(Level.SEVERE, "Can't start web admin console. This functionality will not be available.", e);
        }
    }

    private ByteBuffer fetchResponse(ByteBuffer byteBuffer) {
        String request = new String(byteBuffer.array(), 0, byteBuffer.position());
        String filename = "";
        for (int i = 5; i < request.length(); ++i) {
            if (request.charAt(i) == ' ') {
                filename = request.substring(5, i);
                if (filename.isEmpty()) {
                    filename = "index.html";
                }
                break;
            }
        }
        if (filename.isEmpty()) {
            return fetchFile(byteBuffer, 400, ERROR_400_FILENAME);
        }

        if (request.startsWith(REST_REQUEST_KEY)) {
            return restService.getResponse(byteBuffer);
        }

        return fetchFile(byteBuffer, 200, HTML_ROOT_PATH + filename);
    }

    private ByteBuffer fetchFile(ByteBuffer byteBuffer, int statusCode, String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1);

        Map<String, String> contentTypes = Collections.unmodifiableMap(new HashMap<String, String>(){{
            put("html", "text/html");
            put("js", "application/javascript");
        }});

        try {
            // we can't use RandomAccessFile here bacause we load files from JAR
            //  RandomAccessFile aFile = new RandomAccessFile(filename, "r");
            InputStream resourceAsStream = getClass().getResourceAsStream(filename);
            byte[] content = new byte[FILE_CAPACITY];

            int read;
            int contentLength = 0;

            while ((read = resourceAsStream.read(content, contentLength, content.length - contentLength)) != -1) {
                contentLength += read;
            }
            resourceAsStream.close();

            ResponseHeadersBuilder responseHeadersBuilder = new ResponseHeadersBuilder();
            responseHeadersBuilder
                    .withStaticCode(statusCode)
                    .withContentType(contentTypes.get(extension))
                    .withContentLength(contentLength);

            byteBuffer.clear();
            byteBuffer.put(responseHeadersBuilder.build().getBytes());
            for (int i = 0; i < contentLength; ++i) {
                byteBuffer.put(content[i]);
            }

            byteBuffer.put(new byte[]{CR, LF});
            byteBuffer.flip();
        } catch (IOException e) {
            fetchFile(byteBuffer, 404, ERROR_404_FILENAME);
        }

        return byteBuffer;
    }

    private boolean isHttpRequestReadyForProcessing(ByteBuffer byteBuffer) {
        return (byteBuffer.position() > 3) &&
               (byteBuffer.get(byteBuffer.position()-1) == LF) &&
               (byteBuffer.get(byteBuffer.position()-2) == CR) &&
               (byteBuffer.get(byteBuffer.position()-3) == LF) &&
               (byteBuffer.get(byteBuffer.position()-4) == CR);
    }
}
