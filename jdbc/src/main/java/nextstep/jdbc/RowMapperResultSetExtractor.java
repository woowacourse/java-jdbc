package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperResultSetExtractor<T> {

    private RowMapper<T> rowMapper;

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> extractData(ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();

        int rowNum = 0;
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet, rowNum++));
        }

        return result;
    }
}
