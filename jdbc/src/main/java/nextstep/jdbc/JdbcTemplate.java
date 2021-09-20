package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    protected final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... params) {
        execute(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... params) {
        final StatementCallback<List<T>> callback =
                statement -> new ResultSetExtractor<>(rowMapper).extractData(statement.executeQuery());
        return execute(sql, callback, params);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        final List<T> results = queryForList(sql, rowMapper, params);
        if (results.isEmpty()) {
            throw new IncorrectResultSizeDataAccessException("파라미터에 해당하는 엔티티를 찾을 수 없습니다.");
        }

        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("파라미터와 일치하는 엔티티가 2개 이상입니다.");
        }

        return results.get(0);
    }

    private <T> T execute(String sql, StatementCallback<T> callback, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setValues(statement, params);
            return callback.call(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("SQL 실행 중 에러가 발생했습니다.");
        }
    }

    private void setValues(PreparedStatement statement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}
