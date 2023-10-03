package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.exception.ResultSetTemplateException;

public class ResultSetTemplate {

    public <T> T execute(
            final PreparedStatement preparedStatement,
            final ResultSetMapper<T> resultSetMapper
    ) {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultSetMapper.execute(resultSet);
        } catch (final SQLException e) {
            throw new ResultSetTemplateException(e);
        }
    }
}
