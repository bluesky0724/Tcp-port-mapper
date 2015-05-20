package com.vadim.ok.webadminconsole.server;

public class ResponseHeadersBuilder {
    private final StringBuilder responseHeaders = new StringBuilder();

    public ResponseHeadersBuilder withStaticCode(int statusCode) {
        responseHeaders.append("HTTP/1.1 ");
        responseHeaders.append(statusCode);
        responseHeaders.append(" OK\r\n");
        return this;
    }

    public ResponseHeadersBuilder withContentType(String contentType) {
        responseHeaders.append("Content-Type: ");
        responseHeaders.append(contentType);
        responseHeaders.append("\r\n");
        return this;
    }

    public ResponseHeadersBuilder withContentLength(long contentLength) {
        responseHeaders.append("Content-Length: ");
        responseHeaders.append(contentLength);
        responseHeaders.append("\r\n");
        return this;
    }

    public String build() {
        responseHeaders.append("\r\n");
        return responseHeaders.toString();
    }
}







