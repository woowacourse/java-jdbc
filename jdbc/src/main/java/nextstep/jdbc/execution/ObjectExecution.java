package nextstep.jdbc.execution;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import nextstep.jdbc.RowMapper;

public class ObjectExecution<T> extends AbstractExecution<T> {

    private final RowMapper<T> rowMapper;

    public ObjectExecution(String sql, Object[] arguments, RowMapper<T> rowMapper) {
        super(sql, arguments);
        this.rowMapper = rowMapper;
    }

    @Override
    public T execute(PreparedStatement statement) throws SQLException {
        for (int i = 0; i < arguments.length; i++) {
            statement.setObject(i + 1, arguments[i]);
        }
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return rowMapper.rowMap(resultSet, resultSet.getRow());
            }
        }
        return null;
    }
}
