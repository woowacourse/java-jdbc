package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    public static final String SQL_INFO_LOG = "query : {}";

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, PreparedStatementSetter pss) {
        return executeUpdate(sql, pss::setValues);
    }

    public int update(String sql, Object... args) {
        return executeUpdate(sql, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public int update(String sql, Object[] args, int[] argTypes) {
        return executeUpdate(sql, pstmt -> {
            for (int i = 1; i <= args.length; i++) {
                pstmt.setObject(i, args[i], argTypes[i]);
            }
        });
    }

    private int executeUpdate(String sql, JdbcExecutable je) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            je.execute(pstmt);
            if (log.isDebugEnabled()) {
                log.debug(SQL_INFO_LOG, sql);
            }
            return pstmt.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter setter) {
        return executeQuery(sql, rowMapper, setter::setValues);
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeQuery(sql, rowMapper, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        });
    }

    public <T> T executeQuery(String sql, RowMapper<T> rowMapper, JdbcExecutable je) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (log.isDebugEnabled()) {
                log.debug(SQL_INFO_LOG, sql);
            }
            je.execute(pstmt);
            ResultSet rs = pstmt.executeQuery();


            if (!rs.next()) {
                return null;
            }
            return rowMapper.mapRow(rs);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (log.isDebugEnabled()) {
                log.debug(SQL_INFO_LOG, sql);
            }
            ResultSet rs = pstmt.executeQuery();

            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }
}
