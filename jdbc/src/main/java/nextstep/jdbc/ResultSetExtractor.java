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

    public List<T> extractData(ResultSet rs) throws SQLException {
        var result = new ArrayList<T>();

        int rowNum = 0;
        while (rs.next()) {
            result.add(this.rowMapper.mapRow(rs, rowNum++));
        }
        return result;
    }
}
