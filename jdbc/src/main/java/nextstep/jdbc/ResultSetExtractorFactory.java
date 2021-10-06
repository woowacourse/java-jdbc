package nextstep.jdbc;

import java.util.ArrayList;
import java.util.List;

public class ResultSetExtractorFactory {

    public static <T> ResultSetExtractor<T> objectResultSetExtractor(RowMapper<T> rowMapper) {
        return rs -> {
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        };
    }

    public static <T> ResultSetExtractor<List<T>> listResultSetExtractor(RowMapper<T> rowMapper) {
        return rs -> {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        };
    }

    private ResultSetExtractorFactory() {
    }
}
