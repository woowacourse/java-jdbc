package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class PreparedStatementParamMapping {

    private final Map<String, PreparedStatementSetter> setters = Map.of(
            "java.lang.String", this::setString,
            "java.lang.Integer", this::setInt,
            "java.lang.Long", this::setLong,
            "java.lang.Boolean", this::setBoolean
    );

    public void callSetter(final String typeName, final PreparedStatement pstmt, final int index, final Object value) throws SQLException {
        PreparedStatementSetter setter = setters.get(typeName);

        if (setter == null) {
            throw new RuntimeException("SQL 매핑을 지원하지 않는 타입입니다.");
        }

        setter.execute(pstmt, index, value);
    }

    private void setString(final PreparedStatement pstmt, final int index, final Object value) throws SQLException {
        pstmt.setString(index, (String) value);
    }

    private void setInt(final PreparedStatement pstmt, final int index, final Object value) throws SQLException {
        pstmt.setInt(index, (Integer) value);
    }

    private void setLong(final PreparedStatement pstmt, final int index, final Object value) throws SQLException {
        pstmt.setLong(index, (Long) value);
    }

    private void setBoolean(final PreparedStatement pstmt, final int index, final Object value) throws SQLException {
        pstmt.setBoolean(index, (Boolean) value);
    }
}
