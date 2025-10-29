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
import com.interface21.jdbc.datasource.DataSourceUtils;
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
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (
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

    private int command(final CommandExecution execution, final CommandSpecification specification) {
        return execute(specification.preparedStatementSpecification(), execution::execute);
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

    public int insert(final CommandSpecification specification) {
        return command(new CreateExecution(), specification);
    }

    public int update(final CommandSpecification specification) {
        return command(new UpdateExecution(), specification);
    }

    public <T> Optional<T> findOne(final QuerySpecification<T> specification) {
        return query(new FindOneQueryExecution<>(), specification);
    }

    public <T> List<T> findAll(final QuerySpecification<T> specification) {
        return query(new FindAllQueryExecution<>(), specification);
    }

    private interface PreparedStatementExecutor<T> {

        T process(PreparedStatement preparedStatement) throws SQLException;
    }
}
