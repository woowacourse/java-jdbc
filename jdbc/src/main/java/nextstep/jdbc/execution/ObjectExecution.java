package nextstep.jdbc.execution;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import nextstep.jdbc.RowMapper;
import nextstep.jdbc.execution.support.ArgumentsSetter;

public class ObjectExecution<T> extends AbstractExecution<T> {

    private final RowMapper<T> rowMapper;

    public ObjectExecution(String sql, Object[] arguments, RowMapper<T> rowMapper) {
        super(sql, arguments);
        this.rowMapper = rowMapper;
    }

    @Override
    public T execute(PreparedStatement statement) throws SQLException {
        ArgumentsSetter.setArguments(statement, arguments);

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return rowMapper.rowMap(resultSet, resultSet.getRow());
        }
        return null;
    }
}
