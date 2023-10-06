package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... parameters) {
        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator(sql);
        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter(parameters);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(connection)) {
            log.debug("query : {}", sql);
            preparedStatementSetter.setObjects(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> queryForList(
            String sql, 
            RowMapper<T> rowMapper,
            Object... parameters
    ) {
        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator(sql);
        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter(parameters);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(connection)) {
            log.debug("query : {}", sql);
            preparedStatementSetter.setObjects(preparedStatement);
            
            return getQueryResults(rowMapper, preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> getQueryResults(
            RowMapper<T> rowMapper,
            PreparedStatement preparedStatement
    ) throws SQLException {
        ResultSetExtractor<T> resultSetExtractor = new ResultSetExtractor<>(rowMapper);
        
        return resultSetExtractor.extract(preparedStatement.executeQuery());
    }

    public <T> T queryForObject(
            String sql,
            RowMapper<T> rowMapper,
            Object... parameters
    ) {
        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator(sql);
        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter(parameters);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(connection)) {
            log.debug("query : {}", sql);
            preparedStatementSetter.setObjects(preparedStatement);
            List<T> queryResults = getQueryResults(rowMapper, preparedStatement);

            return validateInquiryResult(queryResults);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T validateInquiryResult(final List<T> queryResults) {
        if (queryResults.size() == 1) {
            return queryResults.get(0);
        }

        throw new DataAccessException("조회 결과가 올바르지 않습니다.");
    }

}
