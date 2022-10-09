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
    private static final int SINGLE_RESULT_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public void update(final String sql, final Object... args) {
        execute(sql, preparedStatement -> {
            setArguments(preparedStatement, args);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> results = query(sql, rowMapper, args);
        return getSingleResult(results);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, preparedStatement -> {
            setArguments(preparedStatement, args);
            return getResult(preparedStatement, rowMapper);
        });
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            log.error("ERROR : {}", e.getMessage());
            throw new DataAccessException(e);
        }
    }

    private <T> T getSingleResult(final List<T> results) {
        if (results.size() != SINGLE_RESULT_SIZE) {
            throw new DataAccessException("조회한 결과의 크기가 1이 아닙니다.");
        }
        return results.get(0);
    }

    private <T> List<T> getResult(final PreparedStatement preparedStatement,
                                  final RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet rs = preparedStatement.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs, rs.getRow()));
            }
            return result;
        }
    }

    private void setArguments(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        int index = SINGLE_RESULT_SIZE;
        for (Object arg : args) {
            pstmt.setObject(index, arg);
            index++;
        }
    }
}
