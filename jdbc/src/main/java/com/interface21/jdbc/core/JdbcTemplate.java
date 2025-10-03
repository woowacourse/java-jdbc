package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object...args) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = prepareStatement(conn, sql, args);
                ResultSet rs = preparedStatement.executeQuery();
        ) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object...args) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = prepareStatement(conn, sql, args);
                ResultSet rs = preparedStatement.executeQuery();
        ) {
            return rowMapper.mapRow(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement prepareStatement(Connection conn, String sql, Object...args)
            throws SQLException {
        final PreparedStatement prepareStatement = conn.prepareStatement(sql);
        for (int i=0; i< args.length; i++) {
            prepareStatement.setObject(i+1, args[i]);
        }
        return prepareStatement;
    }

}
