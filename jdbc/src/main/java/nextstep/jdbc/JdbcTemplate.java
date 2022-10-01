package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private static final int DEFAULT_PARAM_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            setParams(statement, params);
            statement.execute();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            setParams(statement, params);
            return executeQuery(rowMapper, statement);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            setParams(statement, params);
            return executeQueryForList(rowMapper, statement);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T executeQuery(final RowMapper<T> rowMapper, final PreparedStatement statement) {
        T result = null;
        try (final ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                result = rowMapper.map(resultSet);
            }
        } catch (final SQLException e) {
            throw new DataAccessException("query exception!", e);
        }
        return result;
    }

    private static <T> List<T> executeQueryForList(final RowMapper<T> rowMapper, final PreparedStatement statement)
            throws SQLException {
        final List<T> result = new ArrayList<>();
        try (final ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.add(rowMapper.map(resultSet));
            }
        } catch (final SQLException e) {
            throw new DataAccessException("query exception!", e);
        }
        return result;
    }

    private void setParams(final PreparedStatement statement, final Object[] params)
            throws SQLException {
        int paramIndex = DEFAULT_PARAM_INDEX;
        for (final Object param : params) {
            statement.setObject(paramIndex++, param);
        }
    }
}
