package nextstep.jdbc.templates;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import nextstep.jdbc.utils.ConnectionManager;
import nextstep.jdbc.utils.JdbcResourceCloser;
import nextstep.jdbc.utils.preparestatement.PreparedStatementCallback;
import nextstep.jdbc.utils.preparestatement.PreparedStatementCreator;
import nextstep.jdbc.utils.statement.StatementCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(BaseJdbcTemplate.class);

    private final DataSource dataSource;

    public BaseJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected <T> T execute(StatementCallback<T> action) {
        Connection connection = null;
        Statement stmt = null;
        T result = null;
        try {
            connection = ConnectionManager.getConnection(dataSource);
            stmt = connection.createStatement();
            result = action.getResult(stmt);
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
            ConnectionManager.errorHandle();
        } finally {
            JdbcResourceCloser.closeStatement(stmt);
            JdbcResourceCloser.closeConnection(connection);
        }
        return result;
    }

    protected <T> T execute(PreparedStatementCreator preparedStatementCreator,
                            PreparedStatementCallback<T> action) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        T result = null;
        try {
            connection = ConnectionManager.getConnection(dataSource);
            preparedStatement =
                preparedStatementCreator.createPreparedStatement(connection);
            result = action.getResult(preparedStatement);
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
            ConnectionManager.errorHandle();
        } finally {
            JdbcResourceCloser.closePreparedStatement(preparedStatement);
            JdbcResourceCloser.closeConnection(connection);
        }
        return result;
    }
}
