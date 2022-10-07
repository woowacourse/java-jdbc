package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {

    private final RowMapper<T> rowMapper;

    public RowMapperResultSetExtractor(final RowMapper<T> rowMapper) {
        if (rowMapper == null) {
            throw new IllegalArgumentException("RowMapper is required");
        }
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> extractData(final ResultSet resultSet) throws SQLException {
        if (resultSet == null) {
            return Collections.emptyList();
        }

        final List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, rowNum++));
        }
        return results;
    }
}
