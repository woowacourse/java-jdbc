package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResultSetExtractor {

    public static <T> Optional<T> extractForObject(final RowMapper<T> rowMapper, final ResultSet rs)
            throws SQLException {
        if (rs.next()) {
            final T mappingData = rowMapper.mapRow(rs);
            return Optional.of(mappingData);
        }
        return Optional.empty();
    }

    public static <T> List<T> extract(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        final List<T> result = new ArrayList<>();
        while (rs.next()) {
            final T mappingData = rowMapper.mapRow(rs);
            result.add(mappingData);
        }
        return result;
    }
}
