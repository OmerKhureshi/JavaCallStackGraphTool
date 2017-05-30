package com.application.threads;

import java.sql.Timestamp;
import java.time.Instant;

public class CustomLogger {
    public static void main(String[] args) {
        String time_instant = "2017-03-01T21:34:55.549Z";
        Instant instant = Instant.parse(time_instant);
        Timestamp timestamp = Timestamp.from(instant);
        String mills = "1488404095549";
        // java.sql.Timestamp sqlTimeStamp = new Timestamp(instant.toEpochMilli());
        Timestamp sqlTimeStamp = new Timestamp(Long.valueOf(mills));
        System.out.println("In milliseconds: " + instant.toEpochMilli());
        System.out.println("In sql timestamp: " + sqlTimeStamp);

    }
}
