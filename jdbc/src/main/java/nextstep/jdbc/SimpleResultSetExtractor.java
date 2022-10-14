package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimpleResultSetExtractor<T> implements ResultSetExtractor<T> {

    private RowMapper<T> rowMapper;

    public SimpleResultSetExtractor(final RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> extract(final ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();
        if (resultSet.next()) {
            T t = rowMapper.mapRow(resultSet);
            result.add(t);
        }
        return result;
    }
}
