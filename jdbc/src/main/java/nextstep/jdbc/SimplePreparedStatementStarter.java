package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimplePreparedStatementStarter implements PreparedStatementStarter {

    private final PreparedStatement preparedStatement;

    public SimplePreparedStatementStarter(final PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    @Override
    public void setParameters(final Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            this.preparedStatement.setObject(i+1, parameters[i]);
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        return preparedStatement.executeUpdate();
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return preparedStatement.executeQuery();
    }
}
