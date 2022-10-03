package nextstep.jdbc;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;

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

    public void update(final String sql, final Object... parameters) {
        final SqlExecutor<Integer> executor = PreparedStatement::executeUpdate;
        execute(sql, executor, parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final SqlExecutor<T> executor = pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                verifyResultSizeIsOne(rs);
                return rowMapper.mapRow(rs);
            }
        };
        return execute(sql, executor, parameters);
    }

    private void verifyResultSizeIsOne(final ResultSet rs) throws SQLException {
        rs.last();
        final int resultSize = rs.getRow();
        if (resultSize == 0) {
            throw new EmptyResultDataAccessException();
        }
        if (resultSize > 1) {
            throw new IncorrectResultSizeDataAccessException(resultSize);
        }
        rs.first();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final SqlExecutor<List<T>> executor = pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                return collectByRowMapper(rowMapper, rs);
            }
        };
        return execute(sql, executor, parameters);
    }

    private <T> List<T> collectByRowMapper(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        final List<T> result = new ArrayList<>();
        while (rs.next()) {
            final T t = rowMapper.mapRow(rs);
            result.add(t);
        }
        return result;
    }

    private <T> T execute(final String sql, final SqlExecutor<T> executor, final Object... parameters) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)) {
            log.debug("query : {}", sql);

            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }

            return executor.execute(pstmt);

        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcConnectionException("Fail to get JDBC Connection", e);
        }
    }
}
