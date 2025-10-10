package com.interface21.jdbc.core;

import com.interface21.jdbc.core.execution.command.CommandExecution;
import com.interface21.jdbc.core.execution.command.CommandSpecification;
import com.interface21.jdbc.core.execution.command.CreateExecution;
import com.interface21.jdbc.core.execution.command.UpdateExecution;
import com.interface21.jdbc.core.execution.query.*;
import com.interface21.jdbc.core.preparedstatement.PreparedStatementFactory;
import com.interface21.jdbc.core.preparedstatement.PreparedStatementSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public record JdbcTemplate(DataSource dataSource) {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private <T> T execute(
            PreparedStatementSpecification preparedStatementSpecification,
            PreparedStatementExecutor<T> preparedStatementExecutor
    ) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = PreparedStatementFactory.initialize(
                        connection,
                        preparedStatementSpecification
                )
        ) {
            return preparedStatementExecutor.process(preparedStatement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void command(final CommandExecution execution, final CommandSpecification specification) {
        execute(specification.preparedStatementSpecification(), PreparedStatementExecutor.commandExecutor(execution));
    }

    private <T> Optional<T> singleQuery(
            final SingleQueryExecution<T> execution,
            final QuerySpecification<T> specification
    ) {
        return execute(
                specification.preparedStatementSpecification(),
                preparedStatement -> execution.execute(preparedStatement, specification)
        );
    }

    private <T> List<T> multiQuery(
            final MultiQueryExecution<T> execution,
            final QuerySpecification<T> specification
    ) {
        return execute(
                specification.preparedStatementSpecification(),
                preparedStatement -> execution.execute(preparedStatement, specification).stream().toList()
        );
    }

    public void insert(final CommandSpecification specification) {
        command(new CreateExecution(), specification);
    }

    public void update(final CommandSpecification specification) {
        command(new UpdateExecution(), specification);
    }

    public <T> Optional<T> findOne(final QuerySpecification<T> specification) {
        return singleQuery(new FindOneQueryExecution<>(), specification);
    }

    public <T> List<T> findAll(final QuerySpecification<T> specification) {
        return multiQuery(new FindAllQueryExecution<>(), specification);
    }

    private interface PreparedStatementExecutor<T> {

        static <T> PreparedStatementExecutor<T> commandExecutor(CommandExecution execution) {
            return preparedStatement -> {
                execution.execute(preparedStatement);
                return null;
            };
        }

        T process(PreparedStatement preparedStatement) throws SQLException;
    }
}
