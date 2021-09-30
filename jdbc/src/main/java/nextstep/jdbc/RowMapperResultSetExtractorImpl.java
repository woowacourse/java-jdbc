package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperResultSetExtractorImpl<T> implements ResultSetExtractor<List<T>> {

    private RowMapper<T> rowMapper;

    public RowMapperResultSetExtractorImpl(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> extract(ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();

        while (rs.next()) {
            result.add(rowMapper.mapRow(rs));
        }
        return result;
    }
}
