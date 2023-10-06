package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetExtractor<T> {

    private final RowMapper<T> rowMapper;

    public ResultSetExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> extract(ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();

        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }

        return result;
    }

}
