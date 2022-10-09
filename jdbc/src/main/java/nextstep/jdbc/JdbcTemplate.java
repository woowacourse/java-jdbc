package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... parameters) {
        return execute(sql, ps -> {
            getPreparedStatementSetter(parameters).setValues(ps);
            return ps.executeUpdate();
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return execute(sql, ps -> {
            getPreparedStatementSetter(parameters).setValues(ps);
            ResultSet resultSet = ps.executeQuery();
            return RowMapperResultSetExtractor.extractData(resultSet, rowMapper);
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return execute(sql, ps -> {
            ResultSet resultSet = ps.executeQuery();
            return RowMapperResultSetExtractor.extractDataList(resultSet, rowMapper);
        });
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("쿼리를 실행하는 데에 문제가 발생했습니다.");
        }
    }

    private PreparedStatementSetter getPreparedStatementSetter(Object... parameters) {
        return preparedStatement -> {
            for (int i = 1; i <= parameters.length; i++) {
                preparedStatement.setObject(i, parameters[i - 1]);
            }
        };
    }
}
