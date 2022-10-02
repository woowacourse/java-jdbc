package com.techcourse.dao.statement;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.AbstractPreparedStatementExecutor;

public class InsertPreparedStatement extends AbstractPreparedStatementExecutor {

    private final User user;

    public InsertPreparedStatement(final User user) {
        this.user = user;
    }

    @Override
    protected void execute(final PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, user.getAccount());
        preparedStatement.setString(2, user.getPassword());
        preparedStatement.setString(3, user.getEmail());
        preparedStatement.executeUpdate();
    }
}
