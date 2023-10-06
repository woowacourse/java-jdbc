package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.preparestatement.ArgumentsPrepareStatementSetter;
import org.springframework.jdbc.core.preparestatement.PrepareStatementManager;
import org.springframework.jdbc.core.result.ResultMaker;
import org.springframework.jdbc.core.result.ResultValidator;
import org.springframework.jdbc.core.result.RowMapper;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        final PrepareStatementManager prepareStatementManager = new PrepareStatementManager(new ArgumentsPrepareStatementSetter(args));
        try (final PreparedStatement preparedStatement = prepareStatementManager.generate(dataSource.getConnection(), sql)) {
            prepareStatementManager.setValue(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(final Connection connection, final String sql, final Object... args) {
        final PrepareStatementManager prepareStatementManager = new PrepareStatementManager(new ArgumentsPrepareStatementSetter(args));
        try (final PreparedStatement preparedStatement = prepareStatementManager.generate(connection, sql)) {
            prepareStatementManager.setValue(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> result = query(sql, rowMapper, args);
        ResultValidator.validateSingleResult(result);
        return result.iterator().next();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, @Nullable final Object... args) {
        final PrepareStatementManager prepareStatementManager = new PrepareStatementManager(new ArgumentsPrepareStatementSetter(args));
        try (final PreparedStatement preparedStatement = prepareStatementManager.generate(dataSource.getConnection(), sql)) {
            prepareStatementManager.setValue(preparedStatement);

            final ResultMaker resultMaker = new ResultMaker(preparedStatement);
            return resultMaker.extractData(rowMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
