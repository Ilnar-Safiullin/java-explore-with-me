package ru.practicum;

import java.time.format.DateTimeFormatter;

public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
}