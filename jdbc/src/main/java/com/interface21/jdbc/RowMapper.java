package com.interface21.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class RowMapper {

    public static Map<String, Object> mapRowToResult(
            final int columnCount,
            final ResultSetMetaData metaData,
            final ResultSet rs
    ) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < columnCount; i++) {
            String columnName = metaData.getColumnName(i + 1);
            Object value = rs.getObject(columnName);

            result.put(columnName, value);
        }
        return result;
    }
}
