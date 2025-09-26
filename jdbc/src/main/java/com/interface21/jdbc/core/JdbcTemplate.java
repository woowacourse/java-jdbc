package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        testConnection(dataSource);
    }

    public void update(String sql, Object... args) {
        try (var conn = dataSource.getConnection(); var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParameters(pstmt, args);
            pstmt.execute();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T selectOne(RowMapper<T> rowMapper, String sql, Object... args) {
        try (var conn = dataSource.getConnection(); var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParameters(pstmt, args);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        for (int i = 1; i <= args.length; i++) {
            pstmt.setObject(i, args[i - 1]);
        }
    }

    private void testConnection(DataSource dataSource) {
        try (var connection = dataSource.getConnection()) {
            var databaseProductName = connection.getMetaData().getDatabaseProductName();
            log.info("Connection established to database : {}", databaseProductName);

        } catch (NullPointerException e) {
            log.error("Connection is null on dataSource {}", dataSource);
            throw new RuntimeException(e);

        } catch (SQLException e) {
            log.error(e.getMessage(), e.getCause());
            throw new RuntimeException(e);
        }
    }
}
