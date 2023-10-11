package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.exception.DataAccessException;
import org.springframework.jdbc.exception.IncorrectResultSizeDataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, Object... args) {
        context(sql, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rm) {
        return context(sql, new RowByResultSet<>(rm));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rm, Object... args) {
        List<T> list = context(sql, new RowByResultSet<>(rm), args);

        if (list.isEmpty()) {
            return null;
        }

        if (list.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("조건에 해당하는 값이 " + list.size() + "개입니다.");
        }

        return list.get(0);
    }

    private void context(String sql, Object... args) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    private <T> List<T> context(String sql, ResultSetStrategy<List<T>> rss, Object... args) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                return rss.getData(rs);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }
}
