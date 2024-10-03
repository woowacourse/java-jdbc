package com.interface21.jdbc.core;

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

    public int update(String sql, Object... args) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);

            setParameters(pstmt, args);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);

            setParameters(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                if (results.isEmpty()) {
                    return null;
                }
                if (results.size() > 1) {
                    throw new IllegalStateException("Incorrect result size: expected " + 1 + ", actual " + results.size());
                }
                return results.getFirst();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void setParameters(PreparedStatement pstmt, Object... args) {
        for (int parameterIndex = 0; parameterIndex < args.length; parameterIndex++) {
            Object argument = args[parameterIndex];
            int position = parameterIndex + 1;
            try {
                pstmt.setObject(position, argument);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
