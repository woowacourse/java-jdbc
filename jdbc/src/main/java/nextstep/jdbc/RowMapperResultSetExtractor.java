package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;

public class RowMapperResultSetExtractor {

    public static <T> T extractData(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> extracted = extractDataList(rs, rowMapper);

        if (extracted.isEmpty()) {
            throw new EmptyResultDataAccessException("데이터가 존재하지 않습니다.");
        }
        if (extracted.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("원하는 데이터 수와 일치하지 않습니다.");
        }
        return extracted.iterator().next();
    }

    public static <T> List<T> extractDataList(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rowNum++));
        }
        rs.close();
        return results;
    }
}
