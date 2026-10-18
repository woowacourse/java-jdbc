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

    public int update(String sql, Object... args) {

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            bindParams(pstmt, args);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {

        List<T> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
            ){

            log.debug("query: {}", sql);

            bindParams(pstmt, args);

            try (ResultSet rs = pstmt.executeQuery()){
                while (rs.next()) {
                    list.add(rowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return list;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {

        List<T> result = query(sql, rowMapper, args);

        if (result.isEmpty()) {
            return null;
        }

        return result.getFirst();
    }

    private void bindParams(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i+1, args[i]);
        }
    }
}
