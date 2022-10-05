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
    private static final int FIRST_RESULT_INDEX = 0;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return usePreparedStatement(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return usePreparedStatement(sql, pstmt -> mapToList(rowMapper, pstmt), args);
    }

    private <T> List<T> mapToList(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        List<T> results = new ArrayList<>();
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            T result = rowMapper.run(rs);
            results.add(result);
        }

        return results;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> results = query(sql, rowMapper, args);
        return results.get(FIRST_RESULT_INDEX);
    }

    public <T> T usePreparedStatement(final String sql,
                                      final ThrowingFunction<PreparedStatement, T, SQLException> function,
                                      final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            mapParametersToPreparedStatement(preparedStatement, args);
            return function.apply(preparedStatement);
        } catch (final SQLException e) {
            log.error("error: {}", e);
            throw new DataAccessException(e.getMessage(), e);
            // TODO: 더 추상화된 예외 메시지를 사용해야함
        }
    }

    private void mapParametersToPreparedStatement(final PreparedStatement preparedStatement, final Object[] args)
            throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }
}
