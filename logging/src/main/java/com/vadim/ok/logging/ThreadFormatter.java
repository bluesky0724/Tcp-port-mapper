package com.vadim.ok.logging;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

class ThreadFormatter extends SimpleFormatter {
    @Override
    public synchronized String format(LogRecord record) {
        String formattedRecord = super.format(record);
        formattedRecord = record.getLevel() + ": " + Thread.currentThread().getName() + ": " + record.getMessage() + "\n";
//        formattedRecord = Thread.currentThread().getName() + ": " + formattedRecord;
        return formattedRecord;
    }
}
