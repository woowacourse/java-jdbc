package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;

public class ResultSetExtractor {

    public static <T> T extract(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        checkResultSet(rs);
        return rowMapper.mapRow(rs);
    }

    public static <T> List<T> extractList(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();

        while (rs.next()) {
            result.add(rowMapper.mapRow(rs));
        }
        return result;
    }

    private static void checkResultSet(ResultSet rs) throws SQLException {
        rs.last();
        int resultSetRowCount = rs.getRow();

        if (resultSetRowCount == 0) {
            throw new EmptyResultDataAccessException("데이터가 존재하지 않습니다.");
        }
        if (resultSetRowCount > 1) {
            throw new IncorrectResultSizeDataAccessException("데이터의 크기가 적절하지 않습니다.");
        }
        rs.first();
    }
}
