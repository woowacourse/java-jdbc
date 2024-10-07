package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcTemplate.class);

    protected final DataSource dataSource;

    protected AbstractJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update() {
        String query = createQuery();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            log.debug("query : {}", query);
            setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();
}
