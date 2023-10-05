package org.springframework.jdbc.core.statementexecutor;

import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QueryStatementExecutor<T> implements StatementExecutor<T> {

    public static QueryStatementExecutor QUERY_EXECUTOR = new QueryStatementExecutor();

    private QueryStatementExecutor() {
    }

    @Override
    public <T> List<T> execute(final PreparedStatement pstmt, final RowMapper<T> rowMapper) {
        try (
                ResultSet rs = pstmt.executeQuery()
        ) {
            final List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.map(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
