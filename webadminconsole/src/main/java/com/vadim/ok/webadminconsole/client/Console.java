package com.vadim.ok.webadminconsole.client;

public class Console {
    public static native void error(String message) /*-{
        $wnd.console.error(message);
    }-*/;
}
