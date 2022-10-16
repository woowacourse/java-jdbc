package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<T> {

    private final RowMapper<T> rowMapper;

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public T extractData(ResultSet rs) throws SQLException, DataAccessException {
        return rs.next() ? rowMapper.mapRow(rs, 0) : null;
    }

    @Override
    public List<T> extractList(ResultSet rs) throws SQLException, DataAccessException {
        return assembleResult(rs, rowMapper);
    }

    private List<T> assembleResult(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        int rowNum = 0;
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs, rowNum));
            rowNum++;
        }
        return result;
    }
}
