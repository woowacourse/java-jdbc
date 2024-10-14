package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.NoResultFoundException;
import com.interface21.dao.NotSingleResultException;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int execute(PreparedStatementSetter preparedStatementSetter, String query,
            Object... parameters
    ) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            preparedStatementSetter.setParameters(pstmt, parameters);

            log.debug("query : {}", query);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T getResult(PreparedStatementSetter preparedStatementSetter, String query,
            ObjectMapper<T> objectMapper, Object... parameters
    ) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            preparedStatementSetter.setParameters(pstmt, parameters);

            log.debug("query : {}", query);

            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                T result = objectMapper.map(resultSet, resultSet.getRow());
                validateNoRemainResult(resultSet);
                return result;
            }

            throw new NoResultFoundException();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void validateNoRemainResult(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            throw new NotSingleResultException();
        }
    }

    public <T> List<T> getResults(PreparedStatementSetter preparedStatementSetter, String query,
            ObjectMapper<T> objectMapper, Object... parameters
    ) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            preparedStatementSetter.setParameters(pstmt, parameters);

            log.debug("query : {}", query);

            ResultSet resultSet = pstmt.executeQuery();
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(objectMapper.map(resultSet, resultSet.getRow()));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
