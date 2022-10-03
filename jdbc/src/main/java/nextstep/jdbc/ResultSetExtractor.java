package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetExtractor {

    public static <T> List<T> extractData(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> result = new ArrayList<>();
        int rowNum = 0;
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet, rowNum++));
        }
        return result;
    }
}
