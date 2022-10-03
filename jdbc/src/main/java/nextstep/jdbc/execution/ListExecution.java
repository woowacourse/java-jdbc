package nextstep.jdbc.execution;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.RowMapper;

public class ListExecution<T> extends AbstractExecution<List<T>> {

    private final RowMapper<T> rowMapper;

    public ListExecution(String sql, RowMapper<T> rowMapper) {
        super(sql, null);
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> execute(PreparedStatement statement) throws SQLException {
        ResultSet resultSet = statement.executeQuery();
        return mapRows(resultSet);
    }

    private List<T> mapRows(ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.rowMap(resultSet, resultSet.getRow()));
        }
        return results;
    }
}
