package nextstep.jdbc;

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

    public void update(String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameters(args, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(args, preparedStatement);
            return getResult(preparedStatement, new RowMapperResultSetExtractor<>(rowMapper));
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(args, preparedStatement);
            List<T> results = getResult(preparedStatement, new RowMapperResultSetExtractor<>(rowMapper));
            return DataAccessUtils.nullableSingleResult(results);
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    private void setParameters(Object[] args, PreparedStatement preparedStatement) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            preparedStatement.setObject(index++, arg);
        }
    }

    private <T> List<T> getResult(PreparedStatement preparedStatement,
                                  RowMapperResultSetExtractor<T> rowMapperResultSetExtractor) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return rowMapperResultSetExtractor.extractData(resultSet);
        }
    }
}
