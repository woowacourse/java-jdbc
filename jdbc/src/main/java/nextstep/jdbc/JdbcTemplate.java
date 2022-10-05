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

    public int update(final String sql, final Object... args) {
        return update(sql, statement -> setParameters(statement, args));
    }

    public int update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, statement -> {
            preparedStatementSetter.setValues(statement);
            return statement.executeUpdate();
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return execute(sql, statement -> processResult(statement, resultSet -> getObjects(rowMapper, resultSet)));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, statement -> {
            setParameters(statement, args);
            return processResult(statement, resultSet -> getSingleObject(rowMapper, resultSet));
        });
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback) {
        log.debug("query : {}", sql);
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            return callback.doInPreparedStatement(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParameters(final PreparedStatement statement, final Object[] args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            statement.setObject(index, arg);
            index++;
        }
    }

    private <T> T processResult(final PreparedStatement statement, final ResultSetCallback<T> callback) {
        try (final ResultSet resultSet = statement.executeQuery()) {
            return callback.processResult(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> getObjects(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    private <T> T getSingleObject(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            throw new DataAccessException("해당하는 데이터가 없습니다.");
        }
        return rowMapper.mapRow(resultSet);
    }
}
