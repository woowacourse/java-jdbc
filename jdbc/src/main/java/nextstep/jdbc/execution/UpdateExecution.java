package nextstep.jdbc.execution;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateExecution extends AbstractExecution<Void> {

    public UpdateExecution(String sql, Object[] arguments) {
        super(sql, arguments);
    }

    @Override
    public Void execute(PreparedStatement statement) throws SQLException {
        for (int i = 0; i < arguments.length; i++) {
            statement.setObject(i + 1, arguments[i]);
        }
        statement.executeUpdate();
        return null;
    }
}
