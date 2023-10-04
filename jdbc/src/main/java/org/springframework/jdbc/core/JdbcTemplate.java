package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.preparestatement.ArgumentsPrepareStatementSetter;
import org.springframework.jdbc.core.preparestatement.PrepareStatementGenerator;
import org.springframework.jdbc.core.preparestatement.PrepareStatementSetter;
import org.springframework.jdbc.core.result.ResultMaker;
import org.springframework.jdbc.core.result.RowMapper;
import org.springframework.jdbc.core.result.SingleResultValidator;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final PrepareStatementGenerator PREPARED_STATEMENT_GENERATOR = (connection, sql) -> {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        try (final PreparedStatement preparedStatement = PREPARED_STATEMENT_GENERATOR.create(dataSource.getConnection(), sql)) {
            final PrepareStatementSetter psSetter = new ArgumentsPrepareStatementSetter(args);
            psSetter.setValue(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final PreparedStatement preparedStatement = PREPARED_STATEMENT_GENERATOR.create(dataSource.getConnection(), sql)) {
            final PrepareStatementSetter psSetter = new ArgumentsPrepareStatementSetter(args);
            psSetter.setValue(preparedStatement);

            final ResultMaker resultMaker = new ResultMaker(preparedStatement);
            List<T> results = resultMaker.extractData(rowMapper);

            SingleResultValidator.validate(results);
            return results.iterator().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (final PreparedStatement preparedStatement = PREPARED_STATEMENT_GENERATOR.create(dataSource.getConnection(), sql)) {
            final ResultMaker resultMaker = new ResultMaker(preparedStatement);
            return resultMaker.extractData(rowMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
