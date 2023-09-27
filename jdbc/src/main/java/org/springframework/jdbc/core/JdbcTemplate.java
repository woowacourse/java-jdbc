package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, Object...parameters) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameterIfExist(preparedStatement, parameters);
            log.info("JDBC EXECUTE SQL = {}", sql);
            preparedStatement.execute();
        }
    }

    private void setParameterIfExist(PreparedStatement preparedStatement, Object[] parameters) throws SQLException {
        if (parameters.length == 0) {
            return;
        }

        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i+1, parameters[i]);
        }
    }
}
