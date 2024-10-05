package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.mapper.RowMapper;
import com.interface21.jdbc.core.sql.Sql;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(final String sql, final SqlParameterSource parameters) {
        final Sql bindingParametersQuery = new Sql(sql).bindingParameters(parameters);

        Connection conn = null;
        Statement stmt = null;
        try {
            final String value = bindingParametersQuery.getValue();
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate(value);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public <T> T queryForObject(
            final String sql,
            final Map<String, Object> parameters,
            final RowMapper<T> rowMapper
    ) {
        final Sql bindingParametersQuery = new Sql(sql).bindingParameters(parameters);

        Connection conn = null;
        Statement stmt = null;
        try {
            final String value = bindingParametersQuery.getValue();
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            final ResultSet resultSet = stmt.executeQuery(value);
            return rowMapper.mapping(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }
}
