package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.exception.ResultSetMappingFailureException;

public class ResultSetExtractor<T> {

    private final ResultSet resultSet;
    private final RowMapper<T> rowMapper;

    public ResultSetExtractor(ResultSet resultSet, RowMapper<T> rowMapper) {
        this.resultSet = resultSet;
        this.rowMapper = rowMapper;
    }

    public List<T> toList() throws ResultSetMappingFailureException {
        try {
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (SQLException exception) {
            throw new ResultSetMappingFailureException(exception);
        }
    }
}
