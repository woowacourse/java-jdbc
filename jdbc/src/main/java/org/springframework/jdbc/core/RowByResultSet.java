package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowByResultSet<T> implements ResultSetStrategy<List<T>> {

    private final RowMapper<T> rowMapper;

    public RowByResultSet(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> getData(ResultSet rs) throws SQLException {
        List<T> list = new ArrayList<>();

        while (rs.next()) {
            list.add(rowMapper.mapRow(rs));
        }

        return list;
    }
}
