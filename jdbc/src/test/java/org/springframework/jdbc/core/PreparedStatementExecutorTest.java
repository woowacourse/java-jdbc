package org.springframework.jdbc.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.config.TestDataSource;

import java.sql.PreparedStatement;

class PreparedStatementExecutorTest {

    @Test
    void execute() {
        //given
        final PreparedStatementExecutor executor = new PreparedStatementExecutor(TestDataSource.getInstance());
        final String sql = "INSERT INTO tests (name) VALUES (?)";
        final String parameter = "test";

        //when, then
        Assertions.assertDoesNotThrow(() -> {
            executor.execute(
                    PreparedStatement::executeUpdate,
                    sql,
                    parameter
            );
        });
    }
}
