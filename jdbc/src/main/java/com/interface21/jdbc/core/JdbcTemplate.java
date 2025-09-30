package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public void update(
            final String sql,
            final Object... args
    ) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setParameters(pstmt, args);
            pstmt.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setParameters(pstmt, args);
            final List<T> results = queryForList(rowMapper, pstmt);
            if (results.isEmpty()) {
                throw new DataAccessException("No Data");
            }
            return results.getFirst();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setParameters(pstmt, args);
            return queryForList(rowMapper, pstmt);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> queryForList(
            final RowMapper<T> rowMapper,
            final PreparedStatement pstmt
    ) {
        try (ResultSet resultSet = pstmt.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet));
            }
            return results;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(
            final PreparedStatement pstmt,
            final Object[] args
    ) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
