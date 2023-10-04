package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QueryTemplate {

    private static final Logger log = LoggerFactory.getLogger(QueryTemplate.class);

    private final DataSource dataSource;

    public QueryTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T query(String sql, QueryExecutor<T> queryExecutor, Object... parameters) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            settingParameters(pstmt, parameters);
            return queryExecutor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void settingParameters(PreparedStatement pstmt, Object[] parameters) throws SQLException {
        for (int index = 1; index < parameters.length + 1; index++) {
            pstmt.setObject(index, parameters[index - 1]);
        }
    }
}
