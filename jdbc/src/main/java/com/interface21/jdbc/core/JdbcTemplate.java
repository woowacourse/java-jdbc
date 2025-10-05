package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        update(sql, getDefaultPreparedStatementSetter(args));
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        return queryForObject(sql, rowMapper, getDefaultPreparedStatementSetter(args));
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        return query(sql, rowMapper, getDefaultPreparedStatementSetter(args));
    }

    public void update(
            final String sql,
            final PreparedStatementSetter pss
    ) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pss.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter pss
    ) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pss.setValues(pstmt);
            final List<T> results = queryForList(rowMapper, pstmt);
            if (results.isEmpty()) {
                throw new DataAccessException("No Data");
            }
            if (results.size() != 1) {
                throw new DataAccessException("Not only one Data");
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
            final PreparedStatementSetter pss
    ) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pss.setValues(pstmt);
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

    private PreparedStatementSetter getDefaultPreparedStatementSetter(final Object... objects) {
        return pstmt -> {
            for (int i = 0; i < objects.length; i++) {
                pstmt.setObject(i + 1, objects[i]);
            }
        };
    }
}
