package nextstep.jdbc.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperListExtractor<T> implements ResultSetExtractor<List<T>> {

    private final RowMapper<T> rowMapper;

    public RowMapperListExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> extractData(ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            result.add(this.rowMapper.mapRow(rs, rowNum++));
        }
        return result;
    }
}
