package nextstep.jdbc.execution;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.execution.support.ArgumentsSetter;

public class UpdateExecution implements Execution<Integer> {

    private final String sql;
    private final Object[] arguments;

    public UpdateExecution(String sql, Object[] arguments) {
        this.sql = sql;
        this.arguments = arguments;
    }

    @Override
    public Integer execute(PreparedStatement statement) throws SQLException {
        ArgumentsSetter.setArguments(statement, arguments);
        return statement.executeUpdate();
    }

    @Override
    public String getSql() {
        return sql;
    }
}
