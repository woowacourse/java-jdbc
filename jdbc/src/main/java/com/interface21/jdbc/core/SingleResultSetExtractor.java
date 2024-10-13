package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SingleResultSetExtractor<T> implements ResultSetExtractor<T> {

    private final RowMapper<T> rowMapper;

    public SingleResultSetExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public T extract(ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            throw new DataAccessException("결과가 존재하지 않습니다");
        }
        T result = rowMapper.mapRow(resultSet);
        if (resultSet.next()) {
            throw new DataAccessException("2개 이상의 결과가 조회되었습니다");
        }
        return result;
    }
}
