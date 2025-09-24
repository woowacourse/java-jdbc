package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void handleQuery(final String sql, final Consumer<PreparedStatement> statementMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            statementMapper.accept(pstmt);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Object handleQueryAndGet(final String sql, final Consumer<PreparedStatement> statementMapper,
                                    final Function<ResultSet, Object> resultSetMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            statementMapper.accept(pstmt);

            ResultSet rs = pstmt.executeQuery();

            return resultSetMapper.apply(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
