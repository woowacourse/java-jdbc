package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.utils.DataAccessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static int updateCount(Integer integer) {
        return integer;
    }

    public void execute(String sql) {
        try (final Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.info(sql);
            pstmt.execute();
        } catch (SQLException throwables) {
            log.error("execute error");
        }

    }

    public <T> T execute(String sql, PreparedStatementCallback<T> action, Object... args) throws DataAccessException {
        try (final Connection conn = dataSource.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("execute SQLException", e);
        }
    }

    public int update(String sql, Object... args) throws DataAccessException {
        log.info(sql);
        return updateCount(execute(sql, ps -> ps.executeUpdate(), args));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return execute(sql, ps -> {
            try (final ResultSet rs = ps.executeQuery()) {
                List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rowNum++));
                }
                return results;
            }
        }, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.nullableSingleResult(results);
    }
}
