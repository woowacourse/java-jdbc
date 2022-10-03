package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void validateSql(final String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL은 null이거나 빈 값일 수 없습니다.");
        }
    }

    private void setParameters(final PreparedStatement statement, final Object[] parameters)
            throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    public void update(final String sql, final Object... parameters) {
        validateSql(sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement, parameters);
            log.debug("query : {}", sql);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        validateSql(sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(statement, parameters);
            log.debug("query : {}", sql);
            final ResultSet resultSet = statement.executeQuery();

            return mapResultSet(rowMapper, resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> mapResultSet(final RowMapper<T> rowMapper, final ResultSet resultSet)
            throws SQLException {
        final List<T> elements = new ArrayList<>();
        while (resultSet.next()) {
            elements.add(rowMapper.mapRow(resultSet, 0));
        }
        return elements;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters)
            throws DataAccessException {
        final List<T> results = query(sql, rowMapper, parameters);
        if (results.size() == 0) {
            throw new EmptyResultDataAccessException(1);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.iterator().next();
    }
}
