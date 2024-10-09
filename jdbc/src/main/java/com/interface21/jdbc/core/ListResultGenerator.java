package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class ListResultGenerator<T> implements ResultGenerator<T, List<T>> {

    @Override
    public List<T> generate(ResultSetParser<T> parser, ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(parser.parse(resultSet));
        }
        return results;
    }
}


