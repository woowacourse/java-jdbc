package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeException;
import java.sql.Connection;
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

    public void update(String sql, Object... args) {
        update(sql, new ArgumentPreparedStatementSetter(args));
    }

    public void update(String sql, PreparedStatementSetter pstmtSetter) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)
        ) {
            pstmtSetter.setValues(pstmt);

            log.debug("query : {}", sql);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, new ArgumentPreparedStatementSetter(args), rowMapper);
    }

    public <T> List<T> query(String sql, PreparedStatementSetter pstmtSetter, RowMapper<T> rowMapper) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql);
        ) {
            pstmtSetter.setValues(pstmt);

            log.debug("query : {}", sql);

            try (final var resultSet = pstmt.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (resultSet.next()) {
                    list.add(rowMapper.map(resultSet));
                }
                return list;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void update(Connection connection, String sql, Object... args) {
        update(connection, sql, new ArgumentPreparedStatementSetter(args));
    }

    public void update(Connection conn, String sql, PreparedStatementSetter pstmtSetter) {
        try (final var pstmt = conn.prepareStatement(sql)) {
            pstmtSetter.setValues(pstmt);

            log.debug("query : {}", sql);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return queryForObject(sql, new ArgumentPreparedStatementSetter(args), rowMapper);
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter pstmtSetter, RowMapper<T> rowMapper) {
        List<T> resultRows = query(sql, pstmtSetter, rowMapper);
        if (resultRows.size() != 1) {
            throw new IncorrectResultSizeException(1, resultRows.size());
        }
        return resultRows.get(0);
    }
}
