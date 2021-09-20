package nextstep.jdbc.templates;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import nextstep.jdbc.utils.preparestatement.PreparedStatementCallback;
import nextstep.jdbc.utils.preparestatement.PreparedStatementCreator;
import nextstep.jdbc.utils.statement.StatementCallback;

public abstract class BaseJdbcTemplate {

    private final DataSource dataSource;

    public BaseJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected <T> T execute(StatementCallback<T> action) throws SQLException {
        try (Connection con = dataSource.getConnection();
            Statement stmt = con.createStatement()) {

            stmt.setQueryTimeout(30);

            return action.getResult(stmt);
        }
    }

    protected <T> T execute(PreparedStatementCreator preparedStatementCreator,
                            PreparedStatementCallback<T> action) throws SQLException {

        try (Connection con = dataSource.getConnection();
            PreparedStatement preparedStatement =
                preparedStatementCreator.createPreparedStatement(con)) {

            preparedStatement.setQueryTimeout(30);

            return action.getResult(preparedStatement);
        }
    }
}
