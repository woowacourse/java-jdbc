package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static nextstep.jdbc.PreparedStatementValueSetter.setPreparedStatementValues;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    public static final String SQL_INFO_LOG = "query : {}";

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            setPreparedStatementValues(pstmt, pss);
            return pstmt.executeUpdate();
        });
    }

    public int update(String sql, Object... args) {
        return execute(sql, pstmt -> {
            setPreparedStatementValues(pstmt, args);
            return pstmt.executeUpdate();
        });
    }

    public int update(String sql, Object[] args, int[] argTypes) {
        return execute(sql, pstmt -> {
            setPreparedStatementValues(pstmt, args, argTypes);
            return pstmt.executeUpdate();
        });
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter setter) {
        return execute(sql, pstmt -> {
            setPreparedStatementValues(pstmt, setter);
            return ResultSetExtractor.toObject(rowMapper, pstmt);
        });
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, pstmt -> {
            setPreparedStatementValues(pstmt, args);
            return ResultSetExtractor.toObject(rowMapper, pstmt);
        });
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        return execute(sql, pstmt -> ResultSetExtractor.toList(rowMapper, pstmt));
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> executor) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            if (log.isDebugEnabled()) {
                log.debug(SQL_INFO_LOG, sql);
            }
            return executor.execute(pstmt);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }
}
