package org.springframework.jdbc.core.statementexecutor;

import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryForObjectStatementExecutor<T> implements StatementExecutor<T> {

    public static QueryForObjectStatementExecutor QUERY_FOR_OBJECT_EXECUTOR = new QueryForObjectStatementExecutor();

    private QueryForObjectStatementExecutor() {
    }

    @Override
    public <T> T execute(final PreparedStatement pstmt, final RowMapper<T> rowMapper) {
        try (
                ResultSet rs = pstmt.executeQuery()
        ) {
            T result = null;
            if (rs.next()) {
                result = rowMapper.map(rs);
            }
            if (rs.next()) {
                throw new IllegalStateException("2개 이상의 결과가 존재합니다!");
            }
            return (T) result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
