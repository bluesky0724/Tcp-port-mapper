package com.vadim.ok.webadminconsole.client;

import com.google.gwt.core.client.JavaScriptObject;

public class TotalExecutionStatistics extends JavaScriptObject {
    protected TotalExecutionStatistics() {
    }

    public native final String getTotalTransmittedBytesFromClientToTargetCount() /*-{
        return this["totalTransmittedBytesFromClientToTargetCount"];
    }-*/;

    public native final String getTotalTransmittedBytesFromTargetToClientCount() /*-{
        return this["totalTransmittedBytesFromTargetToClientCount"];
    }-*/;

    public native final String getByteBufferCapacity() /*-{
        return this["byteBufferCapacity"];
    }-*/;
}
