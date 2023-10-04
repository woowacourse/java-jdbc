package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int SINGLE_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return executeUpdate(sql, args);
    }

    private int executeUpdate(String sql, Object... args) {
        return execute(new SetPreparedStatementMaker(sql, args), new PreparedStatementUpdateExecuter());
    }

    private <T> T execute(
            PreparedStatementMaker pstmtMaker,
            PreparedStatementExecuter<T> pstmtExecuter
    ) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = pstmtMaker.makePreparedStatement(conn)
        ) {
            return pstmtExecuter.execute(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return executeQuery(sql, rowMapper, args);
    }

    private <T> List<T> executeQuery(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(new SetPreparedStatementMaker(sql, args), new PreparedStatementQueryExecuter<>(rowMapper));
    }

    @Nullable
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        final List<T> objects = query(sql, rowMapper, args);
        return getSingleObject(objects);
    }

    private <T> T getSingleObject(List<T> objects) {
        validateEmpty(objects);
        validateSingleSize(objects);
        return objects.iterator().next();
    }

    private <T> void validateEmpty(List<T> objects) {
        if (objects.isEmpty()) {
            throw new IllegalArgumentException("조회 데이터가 존재하지 않습니다.");
        }
    }

    private <T> void validateSingleSize(List<T> objects) {
        if (objects.size() > SINGLE_SIZE) {
            throw new IllegalArgumentException("조회 데이터가 한 개 이상 존재합니다.");
        }
    }
}
