package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PreparedStatementUtils {

    public static void setParameter(PreparedStatement statement, final Object... args)
            throws SQLException {
        int index = 1;
        for (final Object arg : args) {
            statement.setObject(index++, arg);
        }
    }

    public static <T> List<T> extractData(final ResultSet resultSet, final RowMapper<T> rowMapper)
            throws SQLException {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }
}
