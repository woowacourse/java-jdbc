package nextstep.jdbc;

import nextstep.jdbc.callback.PreparedStatementCallback;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.EmptyResultException;
import nextstep.jdbc.exception.ResultSizeExceedException;
import nextstep.jdbc.setter.ArgumentPreparedStatementSetter;
import nextstep.jdbc.setter.PreparedStatementSetter;
import nextstep.jdbc.setter.SimplePreparedStatementSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    //todo 설정파일에 정의된 dataSource를 가져올 수 없을까...
    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql) {
        executeUpdate(connection -> connection.prepareStatement(sql), new SimplePreparedStatementSetter());
    }

    public void update(String sql, Object... args) {
        executeUpdate(connection -> connection.prepareStatement(sql), new ArgumentPreparedStatementSetter(args));
    }

    //TODO executeUpdate 와 executeQuery의 중복도 줄여보자
    public void executeUpdate(PreparedStatementCallback preparedStatementCallback, PreparedStatementSetter preparedStatementSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = preparedStatementCallback.makePrepareStatement(conn)) {
            preparedStatementSetter.setValues(pstmt);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("executeUpdate Database Access Failed", e);
            throw new DataAccessException("executeUpdate Database Access Failed");
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeQuery(connection -> connection.prepareStatement(sql),
                new ArgumentPreparedStatementSetter(args), new RowMapperResultExtract<>(rowMapper));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = executeQuery(connection -> connection.prepareStatement(sql),
                new ArgumentPreparedStatementSetter(args), new RowMapperResultExtract<>(rowMapper));
        validateSingleResult(results);
        return results.get(0);
    }

    public <T> void validateSingleResult(List<T> results) {
        if (results.isEmpty()) {
            log.error("queryForObject Result is Empty");
            throw new EmptyResultException("queryForObject Result is Empty");
        }
        if (results.size() > 1) {
            log.error("queryForObject Result Size Over than 1, size > {}", results.size());
            throw new ResultSizeExceedException("queryForObject Result Size Over than 1");
        }
    }

    public <T> List<T> executeQuery(PreparedStatementCallback preparedStatementCallback,
                                    PreparedStatementSetter preparedStatementSetter, RowMapperResultExtract<T> rowMapperResultExtract) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = preparedStatementCallback.makePrepareStatement(conn)) {
            preparedStatementSetter.setValues(pstmt);
            List<T> results = rowMapperResultExtract.execute(pstmt);
            return results;
        } catch (SQLException e) {
            log.error("executeQuery Data Access Failed!!", e);
            throw new DataAccessException("executeQuery Data Access Failed!!");
        }
    }
}
