package com.vadim.ok.webadminconsole.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;

public class Table extends CellTable<TotalExecutionStatistics> {
    public Table() {
        setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);

        TextColumn<TotalExecutionStatistics> totalTransmittedBytesFromClientToTarget = new TextColumn<TotalExecutionStatistics>() {
            @Override
            public String getValue(TotalExecutionStatistics object) {
                return object.getTotalTransmittedBytesFromClientToTargetCount();
            }
        };
        addColumn(totalTransmittedBytesFromClientToTarget, "Total Transmitted Bytes from Client to Target, bytes");

        TextColumn<TotalExecutionStatistics> totalTransmittedBytesFromTargetToClientCount = new TextColumn<TotalExecutionStatistics>() {
            @Override
            public String getValue(TotalExecutionStatistics object) {
                return object.getTotalTransmittedBytesFromTargetToClientCount();
            }
        };
        addColumn(totalTransmittedBytesFromTargetToClientCount, "Total Transmitted Bytes from Target to Client, bytes");

        TextColumn<TotalExecutionStatistics> byteBufferCapacity = new TextColumn<TotalExecutionStatistics>() {
            @Override
            public String getValue(TotalExecutionStatistics object) {
                return object.getByteBufferCapacity();
            }
        };
        addColumn(byteBufferCapacity, "Byte Buffer Capacity per Connection, bytes");
    }
}
