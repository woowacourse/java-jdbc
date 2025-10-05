package com.interface21.jdbc.core;

import com.interface21.jdbc.CustomizedDataAccessException;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(
            String sql,
            PreparedStatementSetter setter,
            PreparedStatementCallback<T> action
    ) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            setter.setValues(ps);
            log.info("query : {}", sql);
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            throw new CustomizedDataAccessException(sql, e);
        }
    }

    public void update(
            String sql,
            PreparedStatementSetter setter
    ) {
        execute(sql, setter, ps -> {
            ps.executeUpdate();
            return Optional.empty();
        });
    }

    public <T> Optional<T> queryForObject(
            String sql,
            PreparedStatementSetter setter,
            RowMapper<T> rowMapper
    ) {
        return execute(sql, setter, ps -> {
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                T obj = rowMapper.mapRow(rs);

                if (rs.next()) {
                    throw new CustomizedDataAccessException(
                            sql,
                            new IllegalArgumentException("[ERROR] too many rows (Expected 1 but found 2 or more)")
                    );
                }
                return Optional.of(obj);
            }
        });
    }

    public <T> List<T> queryForObjects(
            String sql,
            PreparedStatementSetter setter,
            RowMapper<T> rowMapper
    ) {
        return execute(sql, setter, ps -> {
            List<T> results = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
            }
            return results;
        });
    }
}
