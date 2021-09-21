package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SelectJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(SelectJdbcTemplate.class);

    public abstract String createQuery();

    public abstract DataSource getDataSource();

    public abstract void setValues(PreparedStatement pstmt) throws SQLException;

    public abstract Object mapRow(ResultSet rs) throws SQLException;

    public Object query() {
        String query = createQuery();
        DataSource dataSource = getDataSource();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)){

            log.debug("query : {}", query);

            setValues(pstmt);
            ResultSet rs = executeQuery(pstmt);

            return mapRow(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }
}
