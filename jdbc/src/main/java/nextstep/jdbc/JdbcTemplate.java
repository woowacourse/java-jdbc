package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nextstep.jdbc.exception.DataAccessException;

public abstract class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    protected abstract DataSource getDataSource();

    public void update(final String sql, Object... args) {
        ArgumentPreparedStatementSetter statementSetter = new ArgumentPreparedStatementSetter(args);

        execute(sql, preparedStatement -> {
            statementSetter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, Object... args) {
        ArgumentPreparedStatementSetter statementSetter = new ArgumentPreparedStatementSetter(args);

        return execute(sql, preparedStatement -> {
            statementSetter.setValues(preparedStatement);
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

    private <T> T execute(String sql, PreparedStatementCallback<T> callback) {
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                 ResultSet.CONCUR_UPDATABLE)
        ) {
            log.debug("query : {}", sql);
            return callback.doInStatement(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("sql 실행 중 에러가 발생하였습니다.");
        }
    }
}
