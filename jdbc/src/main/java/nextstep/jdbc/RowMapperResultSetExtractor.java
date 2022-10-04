package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperResultSetExtractor<T> {

    private final RowMapper<T> rowMapper;

    public RowMapperResultSetExtractor(final RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> extractData(final ResultSet resultSet) throws SQLException {
        List<T> data = new ArrayList<>();
        while(resultSet.next()) {
            data.add(rowMapper.mapRow(resultSet));
        }
        return data;
    }
}
