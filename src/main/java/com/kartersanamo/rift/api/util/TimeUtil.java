package com.kartersanamo.rift.api.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                    .withZone(ZoneOffset.UTC);

    public static String formatUnix(long unixMillis) {
        if (unixMillis == 0) {
            return "None";
        }
        return FORMATTER.format(Instant.ofEpochMilli(unixMillis)) + " UTC";
    }
}