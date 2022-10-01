package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int DEFAULT_PARAM_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            setParams(statement, params);
            statement.execute();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T query(final String sql, final ResultSetFunction<ResultSet, T> function, final Object... params) {
        ResultSet resultSet = null;
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            setParams(statement, params);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return function.apply(resultSet);
            }

            throw new DataAccessException();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (final SQLException ignored) {
            }
        }
    }

    public <T> List<T> queryForList(final String sql, final ResultSetFunction<ResultSet, T> function,
                                    final Object... params) {
        ResultSet resultSet = null;
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            setParams(statement, params);
            resultSet = statement.executeQuery();

            final List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(function.apply(resultSet));
            }

            return result;
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (final SQLException ignored) {
            }
        }
    }

    private void setParams(final PreparedStatement statement, final Object... params)
            throws SQLException {
        int paramIndex = DEFAULT_PARAM_INDEX;
        for (final Object param : params) {
            statement.setObject(paramIndex++, param);
        }
    }
}
