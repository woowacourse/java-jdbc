package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SelectJdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(SelectJdbcTemplate.class);

    protected abstract String createQuery();

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    public Object query() {
        final String sql = createQuery();

        ResultSet resultSet = null;
        try (Connection conn = getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setValues(pstmt);

            LOG.debug("query : {}", sql);
            resultSet = executeQuery(pstmt);
            return mapRow(resultSet);

        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;

    protected abstract Object mapRow(ResultSet resultSet) throws SQLException;

    protected abstract DataSource getDataSource();
}
