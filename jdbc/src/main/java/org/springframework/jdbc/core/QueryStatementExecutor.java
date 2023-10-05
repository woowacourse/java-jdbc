package org.springframework.jdbc.core;

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
    public <T> List<T> execute(final PreparedStatement pstmt, final RowMapper<T> rowMapper) throws SQLException {
        final ResultSet resultSet = pstmt.executeQuery();
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.map(resultSet));
        }
        return results;
    }
}
