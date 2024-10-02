package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
