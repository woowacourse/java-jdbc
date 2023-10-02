package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class QueryTemplate {

    private static final Logger log = LoggerFactory.getLogger(QueryTemplate.class);

    private final DataSource dataSource;

    public QueryTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T query(String sql, QueryExecutor<T> executor, Object... args) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = getInitializedPstmt(sql, conn, args)) {
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement getInitializedPstmt(String sql, Connection conn, Object... args) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);

        initializePstmtArgs(pstmt, args);

        return pstmt;
    }

    private void initializePstmtArgs(PreparedStatement pstmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

}
