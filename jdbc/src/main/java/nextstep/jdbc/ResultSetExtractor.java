package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.exception.ResultSetMappingFailureException;

public class ResultSetExtractor<T> {

    private final RowMapper<T> rowMapper;

    public ResultSetExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public T toObject(ResultSet rs) {
        try {
            if (!rs.next()) {
                return null;
            }
            return rowMapper.mapRow(rs);
        } catch (SQLException exception) {
           throw new ResultSetMappingFailureException(exception.getMessage(), exception.getCause());
        }
    }

    public List<T> toList(ResultSet rs) {
        try {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        } catch (SQLException exception) {
            throw new ResultSetMappingFailureException(exception.getMessage(), exception.getCause());
        }
    }
}
