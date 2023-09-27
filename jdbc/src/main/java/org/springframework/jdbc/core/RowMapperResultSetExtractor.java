package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataAccessException;

public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {

    private final RowMapper<T> rowMapper;

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> extractData(ResultSet rs)
            throws SQLException, DataAccessException {
        List<T> result = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs, rowNum));
            rowNum++;
        }
        return result;
    }
}
