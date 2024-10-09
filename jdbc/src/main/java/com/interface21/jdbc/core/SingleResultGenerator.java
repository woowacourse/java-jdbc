package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SingleResultGenerator<T> implements ResultGenerator<T, T> {

    @Override
    public T generate(ResultSetParser<T> parser, ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            throw new DataAccessException("행이 하나도 조회되지 않았습니다.");
        }
        if (!resultSet.isLast()) {
            throw new DataAccessException("여러개의 행이 조회되었습니다.");
        }
        return parser.parse(resultSet);
    }
}

