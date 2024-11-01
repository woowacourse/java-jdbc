package com.interface21.jdbc.core.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ObjectMapper extends PreparedStatementMapper {

    public ObjectMapper(PreparedStatement preparedStatement, Object[] params) throws SQLException {
        super(preparedStatement, params);
    }

    @Override
    protected void setStatement() throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + DB_INDEX_OFFSET, params[i]);
        }
    }
}
