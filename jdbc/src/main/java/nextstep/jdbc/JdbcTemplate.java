package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.MultipleResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final int SINGLE_COUNT = 1;
    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        execute(sql, preparedStatement -> {
            setParams(preparedStatement, params);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        List<T> result = query(sql, rowMapper, params);
        return getSingleSize(result);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, preparedStatement -> {
            setParams(preparedStatement, params);
            return toList(preparedStatement, rowMapper);
        });
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> preparedStatementCallback) {
        log.debug("query : {}", sql);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return preparedStatementCallback.doProcess(preparedStatement);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(final PreparedStatement preparedStatement, final Object[] params) throws SQLException {
        int size = params.length;
        for (int i = 0; i < size; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }

    private <T> List<T> toList(final PreparedStatement preparedStatement, final RowMapper<T> rowMapper) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    private <T> Optional<T> getSingleSize(final List<T> result) {
        if (result.isEmpty()) {
            return Optional.empty();
        }
        if (result.size() > SINGLE_COUNT) {
            throw new MultipleResultException();
        }
        return Optional.ofNullable(result.get(0));
    }
}
