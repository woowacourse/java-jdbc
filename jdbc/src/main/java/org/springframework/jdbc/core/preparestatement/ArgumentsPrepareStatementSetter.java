package org.springframework.jdbc.core.preparestatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentsPrepareStatementSetter implements PrepareStatementSetter {

    private final Object[] arguments;

    public ArgumentsPrepareStatementSetter(final Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public void setValue(final PreparedStatement preparedStatement) throws SQLException {
        int rowNum = 1;
        for (Object argument : arguments) {
            preparedStatement.setObject(rowNum++, argument);
        }
    }
}
