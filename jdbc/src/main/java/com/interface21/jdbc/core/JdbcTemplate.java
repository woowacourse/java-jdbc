package com.interface21.jdbc.core;

import com.interface21.jdbc.core.execution.command.CommandExecution;
import com.interface21.jdbc.core.execution.command.CommandSpecification;
import com.interface21.jdbc.core.execution.command.CreateExecution;
import com.interface21.jdbc.core.execution.command.UpdateExecution;
import com.interface21.jdbc.core.execution.query.FindAllQueryExecution;
import com.interface21.jdbc.core.execution.query.FindOneQueryExecution;
import com.interface21.jdbc.core.execution.query.QueryExecution;
import com.interface21.jdbc.core.execution.query.QuerySpecification;
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

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void startTransaction(Connection connection) {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commitTransaction(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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

    private <T> T executeWithTransaction(
            PreparedStatementSpecification preparedStatementSpecification,
            PreparedStatementExecutor<T> preparedStatementExecutor,
            Connection externalConnection
    ) {
        try (
                PreparedStatement preparedStatement = PreparedStatementFactory.initialize(
                        externalConnection,
                        preparedStatementSpecification
                )
        ) {
            return preparedStatementExecutor.process(preparedStatement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int command(final CommandExecution execution, final CommandSpecification specification) {
        return execute(specification.preparedStatementSpecification(), execution::execute);
    }

    private int commandWithTransaction(
            final CommandExecution execution,
            final CommandSpecification specification,
            final Connection externalConnection
    ) {
        return executeWithTransaction(
                specification.preparedStatementSpecification(), execution::execute, externalConnection);
    }

    private <T, R> R query(
            final QueryExecution<T, R> execution,
            final QuerySpecification<T> specification
    ) {
        return execute(
                specification.preparedStatementSpecification(),
                preparedStatement -> execution.execute(preparedStatement, specification)
        );
    }

    private <T, R> R queryWithTransaction(
            final QueryExecution<T, R> execution,
            final QuerySpecification<T> specification,
            final Connection externalConnection
    ) {
        return executeWithTransaction(
                specification.preparedStatementSpecification(),
                preparedStatement -> execution.execute(preparedStatement, specification),
                externalConnection
        );
    }

    public int insert(final CommandSpecification specification) {
        return command(new CreateExecution(), specification);
    }

    public int insertWithTransaction(final CommandSpecification specification, Connection externalConnection) {
        return commandWithTransaction(new CreateExecution(), specification, externalConnection);
    }

    public int update(final CommandSpecification specification) {
        return command(new UpdateExecution(), specification);
    }

    public int updateWithTransaction(final CommandSpecification specification, Connection externalConnection) {
        return commandWithTransaction(new UpdateExecution(), specification, externalConnection);
    }

    public <T> Optional<T> findOne(final QuerySpecification<T> specification) {
        return query(new FindOneQueryExecution<>(), specification);
    }

    public <T> Optional<T> findOneWithTransaction(
            final QuerySpecification<T> specification, Connection externalConnection) {
        return queryWithTransaction(new FindOneQueryExecution<>(), specification, externalConnection);
    }

    public <T> List<T> findAll(final QuerySpecification<T> specification) {
        return query(new FindAllQueryExecution<>(), specification);
    }

    public <T> List<T> findAllWithTransaction(
            final QuerySpecification<T> specification, Connection externalConnection) {
        return queryWithTransaction(new FindAllQueryExecution<>(), specification, externalConnection);
    }

    private interface PreparedStatementExecutor<T> {

        T process(PreparedStatement preparedStatement) throws SQLException;
    }
}
