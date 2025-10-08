package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QueryListCallback<T> implements JdbcCallback<List<T>> {

    private final RowMapper<T> rowMapper;

    @Override
    public List<T> call(PreparedStatement pstmt) throws SQLException {
        final List<T> results = new ArrayList<>();
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                T result = rowMapper.call(rs);
                results.add(result);
            }
        }
        return results;
    }
}
