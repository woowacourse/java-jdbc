package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ResultSetExtractor {

    public static <T> T extract(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rowMapper.mapRow(rs);
        }
        return null;
    }

    public static <T> List<T> extractList(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();

        while (rs.next()) {
            result.add(rowMapper.mapRow(rs));
        }
        return result;
    }
}
