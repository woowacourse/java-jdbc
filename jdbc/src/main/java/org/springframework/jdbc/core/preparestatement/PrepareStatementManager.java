package org.springframework.jdbc.core.preparestatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PrepareStatementManager {

    private final PrepareStatementSetter prepareStatementSetter;

    public PrepareStatementManager(final PrepareStatementSetter prepareStatementSetter) {
        this.prepareStatementSetter = prepareStatementSetter;
    }

    public PreparedStatement generate(final Connection connection, final String sql) {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValue(final PreparedStatement preparedStatement) throws SQLException {
        prepareStatementSetter.setValue(preparedStatement);
    }
}
