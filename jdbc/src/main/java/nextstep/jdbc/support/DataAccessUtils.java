package nextstep.jdbc.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.RowMapper;
import nextstep.jdbc.exception.EmptyResultDataAccessException;

public class DataAccessUtils {

    public static <T> T objectResult(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        if (!rs.next()) {
            throw new EmptyResultDataAccessException(1);
        }
        return rowMapper.mapRow(rs, 1);
    }

    public static <T> List<T> listResult(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        final List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs, rs.getRow()));
        }
        return result;
    }
}
