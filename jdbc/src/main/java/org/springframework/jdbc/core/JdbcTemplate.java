package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int MAX_ROW_COUNT = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.size() > MAX_ROW_COUNT) {
            throw new IncorrectResultSizeDataAccessException();
        }

        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement psmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setArgs(args, psmt);
            final List<T> results = getResults(rowMapper, psmt);

            return results;
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> getResults(final RowMapper<T> rowMapper, final PreparedStatement psmt) throws SQLException {
        try (final ResultSet resultSet = psmt.executeQuery()){
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }

            return results;
        }
    }

    private void setArgs(final Object[] args, final PreparedStatement psmt) throws SQLException {
        for (int i = 0; i< args.length; i++) {
            psmt.setObject(i + 1, args[i]);
        }
    }

    public void update(final Connection connection, final String sql, final Object... args) {
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setArgs(args, pstmt);
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
