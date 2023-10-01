package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 데이터 row입니다.");
        }

        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement psmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setArgs(args, psmt);
            final ResultSet resultSet = psmt.executeQuery();

            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }

            return results;
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setArgs(final Object[] args, final PreparedStatement psmt) throws SQLException {
        for (int i = 0; i< args.length; i++) {
            psmt.setObject(i + 1, args[i]);
        }
    }

    public void update(final String sql, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setArgs(args, pstmt);
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
