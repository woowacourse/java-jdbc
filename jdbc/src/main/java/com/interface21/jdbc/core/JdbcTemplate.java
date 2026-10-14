package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(
            String sql,
            List<Object> params
    ) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            setParams(params, statement);
            log.debug("query : {}", sql);

            statement.executeUpdate();
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new RuntimeException(exception);
        }
    }

    public <T> T select(
            String sql,
            ResultSetParsing<T> parsing
    ) {
        return select(sql, List.of(), parsing);
    }

    public <T> T select(
            String sql,
            List<Object> params,
            ResultSetParsing<T> parsing
    ) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            setParams(params, statement);
            log.debug("query : {}", sql);

            ResultSet resultSet = statement.executeQuery();
            return parsing.parse(resultSet);
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new RuntimeException(exception);
        }
    }

    private void setParams(
            List<Object> params,
            PreparedStatement statement
    ) throws SQLException {
        for (int i = 1; i <= params.size(); i++) {
            statement.setObject(i, params.get(i - 1));
        }
    }

    @FunctionalInterface
    public interface ResultSetParsing<T> {
        T parse(ResultSet resultSet) throws SQLException;
    }
}
