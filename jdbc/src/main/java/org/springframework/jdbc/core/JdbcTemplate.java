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

    public void executeQuery(final String sql, SQLParameters parameters) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                log.debug("query : {}", sql);
                parameters.getParameters().forEach(p -> {
                    try {
                        pstmt.setObject(parameters.getParameters().indexOf(p) + 1, p);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                pstmt.executeUpdate();
            }
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, SQLParameters parameters) throws SQLException {
        try (final Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
                parameters.getParameters().forEach(p -> {
                    try {
                        pstmt.setObject(parameters.getParameters().indexOf(p) + 1, p);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                try (final ResultSet rs = pstmt.executeQuery()) {
                    log.debug("query : {}", sql);
                    if (rs.next()) {
                        return rowMapper.mapRow(rs, rs.getRow());
                    }
                    return null;
                }
            }
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) throws SQLException {
        try (final Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
                List<T> result = new ArrayList<>();
                try (final ResultSet rs = pstmt.executeQuery()) {
                    log.debug("query : {}", sql);
                    while (rs.next()) {
                        result.add(rowMapper.mapRow(rs, rs.getRow()));
                    }
                }
                return result;
            }
        }
    }
}
