package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SingleResultSetExtractor<T> {

    private final RowMapper<T> rowMapper;

    public SingleResultSetExtractor(final RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public Optional<T> extractData(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return Optional.of(this.rowMapper.mapRow(rs, 0));
        }
        return Optional.empty();
    }
}
