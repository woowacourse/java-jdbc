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
import org.springframework.lang.Nullable;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, @Nullable final Object... args) {
        final ResultSetMapper<List<T>> resultSetMapper = (rs) -> {
            final List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                rowNum += 1;
                final T row = rowMapper.mapToRow(rs, rowNum);
                results.add(row);
            }
            return results;
        };
        final PreparedStatementExecutor<List<T>> preparedStatementExecutor = (preparedStatement) ->
                mapToResult(preparedStatement.executeQuery(), resultSetMapper);
        return execute(sql, args, preparedStatementExecutor);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, @Nullable final Object... args) {
        final ResultSetMapper<T> resultSetMapper = (rs) -> {
            final int rowCount = rs.getRow();
            if (rowCount > 1) {
                throw new DataAccessException("1개보다 많은 값이 존재합니다.");
            }
            if (rs.next()) {
                return rowMapper.mapToRow(rs, 1);
            }
            return null;
        };
        final PreparedStatementExecutor<T> preparedStatementExecutor = (preparedStatement) ->
                mapToResult(preparedStatement.executeQuery(), resultSetMapper);
        return execute(sql, args, preparedStatementExecutor);
    }

    public int update(final String sql, @Nullable final Object... args) {
        return execute(sql, args, PreparedStatement::executeUpdate);
    }

    private PreparedStatement createPreparedStatement(final Connection conn, final String sql,
                                                      @Nullable final Object[] args) throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        if (args == null) {
            return pstmt;
        }
        for (int i = 1; i <= args.length; i++) {
            pstmt.setObject(i, args[i - 1]);
        }
        return pstmt;
    }

    private <T> T execute(final String sql, @Nullable final Object[] args,
                          final PreparedStatementExecutor<T> preparedStatementExecutor) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = createPreparedStatement(conn, sql, args)) {
            log.debug("query : {}", sql);
            return preparedStatementExecutor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T mapToResult(final ResultSet resultSet, final ResultSetMapper<T> resultSetMapper) {
        try (resultSet) {
            return resultSetMapper.mapToResult(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
