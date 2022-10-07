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

    public int update(String sql, Object... values) {
        return execute(sql, preparedStatement -> {
            setParameters(preparedStatement, values);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return execute(sql, (preparedStatement -> {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return getResult(rowMapper, resultSet);
            }
        }));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... values) {
        return execute(sql, (preparedStatement -> {
            setParameters(preparedStatement, values);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> result = getResult(rowMapper, resultSet);
                return result.get(0);
            }
        }));
    }

    private <T> T execute(String sql, Executable<T> executable) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            return executable.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("sql을 실행할 수 없습니다.");
        }
    }

    private <T> List<T> getResult(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }

    private void setParameters(PreparedStatement preparedStatement, Object[] values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setObject(i + 1, values[i]);
        }
    }
}
