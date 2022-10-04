package nextstep.jdbc.execution;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.RowMapper;
import nextstep.jdbc.execution.support.ArgumentsSetter;

public class QueryExecution<T> extends AbstractExecution<List<T>> {

    private final RowMapper<T> rowMapper;

    public QueryExecution(String sql, Object[] arguments, RowMapper<T> rowMapper) {
        super(sql, arguments);
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> execute(PreparedStatement statement) throws SQLException {
        ArgumentsSetter.setArguments(statement, arguments);
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
