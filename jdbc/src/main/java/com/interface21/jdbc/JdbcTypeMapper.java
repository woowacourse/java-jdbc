package com.interface21.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public enum JdbcTypeMapper {

    STRING(String.class) {
        @Override
        public void map(PreparedStatement ps, int idx, Object value) throws SQLException {
            ps.setString(idx, (String) value);
        }
    },
    LONG(Long.class) {
        @Override
        public void map(PreparedStatement ps, int idx, Object value) throws SQLException {
            ps.setLong(idx, (Long) value);
        }
    };

    private final Class<?> classType;

    JdbcTypeMapper(Class<?> classType) {
        this.classType = classType;
    }

    public static JdbcTypeMapper fromClassType(Object param) {
        return Arrays.stream(values())
                .filter(mapper -> mapper.classType.equals(param.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Wrong type for preparedStatement"));
    }

    public abstract void map(PreparedStatement ps, int idx, Object value) throws SQLException;
}
