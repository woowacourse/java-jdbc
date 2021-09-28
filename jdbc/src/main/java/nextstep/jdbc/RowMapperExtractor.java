package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperExtractor<T> {

    private final RowMapper<T> rowMapper;

    public RowMapperExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> extractData(ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            result.add(this.rowMapper.mapRow(rs, rowNum++));
        }
        return result;
    }
}
