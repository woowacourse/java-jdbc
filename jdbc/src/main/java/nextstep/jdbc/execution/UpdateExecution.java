package nextstep.jdbc.execution;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.execution.support.ArgumentsSetter;

public class UpdateExecution extends AbstractExecution<Integer> {

    public UpdateExecution(String sql, Object[] arguments) {
        super(sql, arguments);
    }

    @Override
    public Integer execute(PreparedStatement statement) throws SQLException {
        ArgumentsSetter.setArguments(statement, arguments);
        return statement.executeUpdate();
    }
}
