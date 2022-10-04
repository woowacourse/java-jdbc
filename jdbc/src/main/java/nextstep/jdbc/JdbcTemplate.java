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

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Parameters parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameter(statement, parameters);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final Parameters parameters, final RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameter(statement, parameters);
            return executeQueryForObject(statement, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T executeQueryForObject(final PreparedStatement statement, final RowMapper<T> rowMapper) {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
        throw new IllegalStateException();
    }

    public <T> List<T> query(final String sql, final Parameters parameters, final RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameter(statement, parameters);
            return executeQuery(statement, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> executeQuery(final PreparedStatement statement, final RowMapper<T> rowMapper) {
        try (ResultSet resultSet = statement.executeQuery()) {
            List<T> result = new ArrayList<>();
            if (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParameter(final PreparedStatement statement, final Parameters parameters) throws SQLException {
        int index = 1;
        for (Object parameter : parameters.getParameters()) {
            statement.setObject(index++, parameter);
        }
    }
}
