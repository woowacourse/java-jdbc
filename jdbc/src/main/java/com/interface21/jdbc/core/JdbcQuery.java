package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcQuery<T> extends JdbcOperation<List<T>> {

    private final RowMapper<T> rowMapper;

    public JdbcQuery(DataSource dataSource, RowMapper<T> rowMapper) {
        super(dataSource);
        this.rowMapper = rowMapper;
    }

    @Override
    protected List<T> executeStatement(PreparedStatement pstmt) throws SQLException {
        List<T> results = new ArrayList<>();

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
        }

        return results;
    }
}
