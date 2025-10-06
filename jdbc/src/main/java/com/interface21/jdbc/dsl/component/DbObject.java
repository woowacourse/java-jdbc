package com.interface21.jdbc.dsl.component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DbObject {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Object object;

    public DbObject(Object object) {
        this.object = object;
    }

    @Override
    public String toString() {
        if (object instanceof String) {
            return "'" + object + "'";
        } else if (object instanceof LocalDateTime localDateTime) {
            String result = DATE_TIME_FORMATTER.format(localDateTime);
            return "'" + result + "'";
        } else {
            return object.toString();
        }
    }
}
