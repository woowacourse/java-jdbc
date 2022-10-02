package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public void update(final String sql, Object... args) {
        execute(sql, preparedStatement -> {
            setStatement(preparedStatement, args);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, preparedStatement -> {
            setStatement(preparedStatement, args);
            ResultSet rs = preparedStatement.executeQuery();
            return ResultSetExtractor.extract(rowMapper, rs);
        });
    }

    public <T> List<T> query(final String sql, RowMapper<T> rowMapper) {
        return execute(sql, preparedStatement -> {
            ResultSet rs = preparedStatement.executeQuery();
            return ResultSetExtractor.extractList(rowMapper, rs);
        });
    }

    private void setStatement(final PreparedStatement statement, final Object[] data) throws SQLException {
        for (int i = 0; i < data.length; i++) {
            statement.setObject(i + 1, data[i]);
        }
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            return callback.doInStatement(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("sql 실행 중 에러가 발생하였습니다.");
        }
    }
}
