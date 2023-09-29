package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            for (int parameterIndex = 0; parameterIndex < args.length; parameterIndex++) {
                pstmt.setString(parameterIndex + 1, (String) args[parameterIndex]);
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {

        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            for (int parameterIndex = 0; parameterIndex < args.length; parameterIndex++) {
                pstmt.setString(parameterIndex + 1, (String) args[parameterIndex]);
            }

            final ResultSet resultSet = pstmt.executeQuery();
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
