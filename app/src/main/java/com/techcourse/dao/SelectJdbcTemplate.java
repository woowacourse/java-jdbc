package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SelectJdbcTemplate {

    protected static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    protected abstract String createQuery();

    protected abstract DataSource getDatasource();

    protected abstract Object mapRow(ResultSet rs);

    protected abstract void setValues(PreparedStatement pstmt);

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    Object query() throws SQLException {
        String sql = createQuery();
        Connection conn = getDatasource().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        setValues(pstmt);
        ResultSet rs = executeQuery(pstmt);

        try (conn; pstmt; rs) {
            log.debug("query : {}", sql);

            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        }
    }

}
