package com.vadim.ok.webadminconsole.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.ArrayList;
import java.util.List;

public class Main implements EntryPoint {
    @Override
    public void onModuleLoad() {
        final Table table = new Table();


        final List<TotalExecutionStatistics> data = new ArrayList<>();

        new DataSource("/rest/"){
            @Override
            public void onSuccess(TotalExecutionStatistics totalExecutionStatistics) {
                data.clear();
                data.add(totalExecutionStatistics);
                table.setRowCount(data.size(), true);
                table.setRowData(0, data);
            }
        }.startPolling(2000);

        RootPanel.get().add(table);
        RootPanel.get().add(new Label("Updated every 2 seconds"));
    }
}
