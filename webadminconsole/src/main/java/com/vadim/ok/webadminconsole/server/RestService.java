package com.vadim.ok.webadminconsole.server;

import com.vadim.ok.portmapper.statistics.TotalExecutionStatistics;

import java.net.HttpURLConnection;
import java.nio.ByteBuffer;

public class RestService {

    private final TotalExecutionStatistics totalExecutionStatistics;

    public RestService(TotalExecutionStatistics totalExecutionStatistics) {
        this.totalExecutionStatistics = totalExecutionStatistics;
    }

    public ByteBuffer getResponse(ByteBuffer byteBuffer) {
        JsonObjectBuilder jsonObjectBuilder = new JsonObjectBuilder();
        jsonObjectBuilder
                .withProperty("byteBufferCapacity", String.valueOf(totalExecutionStatistics.getByteBufferCapacity()))
                .withProperty("totalTransmittedBytesFromClientToTargetCount", String.valueOf(totalExecutionStatistics.getTotalTransmittedBytesFromClientToTargetCount()))
                .withProperty("totalTransmittedBytesFromTargetToClientCount", String.valueOf(totalExecutionStatistics.getTotalTransmittedBytesFromTargetToClientCount()));

        String jsonObject = jsonObjectBuilder.build();

        ResponseHeadersBuilder responseHeadersBuilder = new ResponseHeadersBuilder();
        responseHeadersBuilder
                .withStaticCode(HttpURLConnection.HTTP_OK)
                .withContentLength(jsonObject.length())
                .withContentType("application/json");

        String responseHeaders = responseHeadersBuilder.build();

        String response = responseHeaders.concat(jsonObject);

        return ByteBuffer.wrap(response.getBytes());
    }
}
