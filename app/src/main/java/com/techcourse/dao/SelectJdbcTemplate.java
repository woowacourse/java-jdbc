package com.techcourse.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class SelectJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(SelectJdbcTemplate.class);

    public Object query() {
        final var sql = createQuery();

        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setValues(pstmt);

            List<Object> results = new ArrayList<>();
            try(ResultSet rs = executeQuery(pstmt)) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    protected abstract Object mapRow(ResultSet rs) throws SQLException;

    protected abstract String createQuery();

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;

    protected abstract DataSource getDataSource();
}
