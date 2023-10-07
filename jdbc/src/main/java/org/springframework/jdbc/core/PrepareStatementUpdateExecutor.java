package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PrepareStatementUpdateExecutor implements PrepareStatementExecutor<Integer> {

    @Override
    public Integer execute(PreparedStatement preparedStatement) throws SQLException {
        return preparedStatement.executeUpdate();
    }
}
