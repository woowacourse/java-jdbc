package nextstep.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultListExtractorFromRowMapper<T> implements ResultSetExtractor<List<T>> {
    private final RowMapper<T> rowMapper;

    public ResultListExtractorFromRowMapper(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> extractData(ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (resultSet.next()) {
            results.add(this.rowMapper.mapRow(resultSet, rowNum++));
        }
        return results;
    }
}
