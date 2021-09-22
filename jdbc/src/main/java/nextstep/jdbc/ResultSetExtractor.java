package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetExtractor<T> {

    private final RowMapper<T> rowMapper;

    public ResultSetExtractor(RowMapper rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> extract(ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, rowNum++));
        }
        return results;
    }

}
