package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.jdbc.jdbcparam.JdbcParamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int INITIAL_PARAM_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        runContext(sql, statement -> {
            setParams(statement, params);
            return statement.executeUpdate();
        });
    }

    private Object runContext(final String sql, JdbcAction action) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return action.doAction(statement);
        } catch (SQLException exception) {
            log.warn("SQL Exception alert!!! : {}", exception.getMessage(), exception);
            return Optional.empty();
        }
    }

    private void setParams(final PreparedStatement statement, final Object... params) throws SQLException {
        int paramIndex = INITIAL_PARAM_INDEX;
        for (Object param : params) {
            JdbcParamType.setParam(statement, paramIndex, param);
            paramIndex++;
        }
    }

    public ResultSet find(final String sql, Object... params) {
        return (ResultSet) runContext(sql, statement -> {
            setParams(statement, params);
            return statement.executeQuery();
        });
    }
}
