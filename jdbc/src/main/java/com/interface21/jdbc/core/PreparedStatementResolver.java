package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementResolver {

    public PreparedStatement resolve(PreparedStatement preparedStatement, Object... parameters) {
        try {
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            return preparedStatement;
        } catch (SQLException sqlException) {
            throw new DataAccessException("PreparedStatement 구성 과정에서 문제가 발생했습니다.", sqlException);
        }
    }
}
