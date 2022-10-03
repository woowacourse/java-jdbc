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

    private static final int SINGLE_COUNT = 1;
    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        log.debug("query : {}", sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParams(preparedStatement, params);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        log.debug("query : {}", sql);
        return execute(sql, rowMapper, params);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        log.debug("query : {}", sql);

        List<T> result = execute(sql, rowMapper, params);
        validateResultSize(result);
        return result.get(0);
    }

    private <T> List<T> execute(final String sql, final RowMapper<T> rowMapper, final Object[] params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParams(preparedStatement, params);
            return toList(preparedStatement.executeQuery(), rowMapper);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(final PreparedStatement preparedStatement, final Object[] params) throws SQLException {
        int size = params.length;
        for (int i = 0; i < size; i++) {
            setObject(preparedStatement, i + 1, params[i]);
        }
    }

    private void setObject(final PreparedStatement preparedStatement, final int index, final Object param)
            throws SQLException {
        if (param instanceof String) {
            preparedStatement.setString(index, (String) param);
            return;
        }

        if (param instanceof Long) {
            preparedStatement.setLong(index, (Long) param);
        }
    }

    private <T> List<T> toList(final ResultSet resultSet, final RowMapper<T> rowMapper) {
        try (resultSet) {
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.map(resultSet));
            }
            return result;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    private <T> void validateResultSize(final List<T> result) {
        if (result.size() != SINGLE_COUNT) {
            throw new DataAccessException("조회 결과 값이 여러개 존재합니다.");
        }
    }
}
