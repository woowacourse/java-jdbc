package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultMaker {

    private final PreparedStatement preparedStatement;

    public ResultMaker(final PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public <T> List<T> extractData(final RowMapper<T> rowMapper) throws SQLException {
        final ResultSet resultSet = preparedStatement.executeQuery();
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, rowNum++));
        }
        return results;
    }
}
