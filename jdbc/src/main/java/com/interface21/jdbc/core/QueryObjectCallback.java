package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QueryObjectCallback<T> implements JdbcCallback<T> {

    private final RowMapper<T> rowMapper;

    @Override
    public T call(PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rowMapper.call(rs);
            }
            throw new DataAccessException("No data found");
        }
    }
}
