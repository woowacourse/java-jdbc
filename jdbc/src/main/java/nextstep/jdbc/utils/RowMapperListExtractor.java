package nextstep.jdbc.utils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.templates.JdbcException;

public class RowMapperListExtractor<T> {

    private final RowMapper<T> rowMapper;

    public RowMapperListExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> extractData(ResultSet rs) {
        try {
            List<T> result = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                result.add(this.rowMapper.mapRow(rs, rowNum++));
            }
            return result;
        } catch (Exception e) {
            throw new JdbcException();
        }
    }
}
