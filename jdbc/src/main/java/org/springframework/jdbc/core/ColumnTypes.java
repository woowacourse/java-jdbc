package org.springframework.jdbc.core;

public class ColumnTypes {

    /**
     * ref java.sql.Types
     */
    public static Class<?> convertToClass(int types) {
        switch (types) {
            case -5:
                return long.class;
            case 1:
                return String.class;
            case 12:
                return String.class;
            default:
                throw new IllegalStateException();
        }
    }
}
