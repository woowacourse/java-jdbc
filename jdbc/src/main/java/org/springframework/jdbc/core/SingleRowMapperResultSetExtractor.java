package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;

public class SingleRowMapperResultSetExtractor<T> implements ResultSetExtractor<T> {

    private final RowMapper<T> rowMapper;

    public SingleRowMapperResultSetExtractor(final RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public T extractData(final ResultSet resultSet) throws SQLException, DataAccessException {
        if(resultSet.next()) {
            return rowMapper.mapRow(resultSet);
        }
        throw new DataAccessException("데이터를 찾을 수 없습니다.");
    }
}
