package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetExtractor<T> {

    private final ResultSet rs;
    private final RowMapper<T> rowMapper;

    public ResultSetExtractor(final ResultSet rs, final RowMapper<T> rowMapper) {
        this.rs = rs;
        this.rowMapper = rowMapper;
    }

    public List<T> extractData() throws SQLException {
        final var results = new ArrayList<T>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
        return results;
    }
}
