package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetExtractor<T> {

    private final RowMapper<T> rowMapper;

    public ResultSetExtractor(final RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> extractData(final ResultSet resultSet) throws SQLException {
        final List<T> result = new ArrayList<>();
        int rowNum = 0;
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet, rowNum++));
        }
        return result;
    }
}
