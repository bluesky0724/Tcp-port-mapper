package com.vadim.ok.portmapper.tasks;

import com.vadim.ok.portmapper.config.ServiceDescriptor;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

class HttpHeadersMapper {
    private static final List<String> POSSIBLE_LOCAL_HOST_ADDRESSES = Collections.unmodifiableList(asList(
            "localhost",
            "127.0.0.1"
    ));

    private static final String HOST_HTTP_HEADER_TEMPLATE = "Host: %s:%s";


    public static ByteBuffer mapHttpHeaders(ByteBuffer originalByteBuffer, ServiceDescriptor serviceDescriptor) {
        String requestString = new String(originalByteBuffer.array(), 0, originalByteBuffer.position());
        for (final String localHostAddress : POSSIBLE_LOCAL_HOST_ADDRESSES) {
            String wrongHeader = String.format(HOST_HTTP_HEADER_TEMPLATE, localHostAddress, serviceDescriptor.getLocalPort());
            String rightHeader = String.format(HOST_HTTP_HEADER_TEMPLATE, serviceDescriptor.getRemoteHost(), serviceDescriptor.getRemotePort());
            requestString = requestString.replace(wrongHeader, rightHeader);
        }

        return ByteBuffer.wrap(requestString.getBytes());
    }
}
