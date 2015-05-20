package com.vadim.ok.webadminconsole.client;

import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class DataSource implements AsyncCallback<TotalExecutionStatistics> {
    private final String url;

    public DataSource(String url) {
        this.url = url;
    }

    @Override
    public void onFailure(Throwable caught) {
        Window.alert("Can't establish connection with the server: " + caught.getMessage());
    }

    public void startPolling(int millis) {
        Timer timer = new Timer(){
            @Override
            public void run() {

                RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
                requestBuilder.setCallback(new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() == Response.SC_OK) {
                            JSONValue jsonValue = JSONParser.parseStrict(response.getText());
                            TotalExecutionStatistics totalExecutionStatistics = jsonValue.isObject().getJavaScriptObject().cast();
                            onSuccess(totalExecutionStatistics);

                        } else {
                            Console.error("Can't get data from the server");
                        }
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        Console.error("Can't establish connection with the server");
                    }
                });
                try {
                    requestBuilder.send();
                } catch (RequestException e) {
                    onFailure(e);
                }
            }
        };

        timer.run();
        timer.scheduleRepeating(millis);
    }
}
