package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTemplate {

    private static final int SQL_PARAMETER_START_INDEX = 1;

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql) throws DataAccessException {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> int update(final String sql, final List<T> parameters) throws DataAccessException {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(preparedStatement, parameters);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper)
        throws DataAccessException {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return mapOneResult(preparedStatement.executeQuery(), rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T, S> S queryForObject(final String sql, final List<T> parameters,
                                   final RowMapper<S> rowMapper) throws DataAccessException {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(preparedStatement, parameters);
            return mapOneResult(preparedStatement.executeQuery(), rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> void setParameters(final PreparedStatement preparedStatement,
                                   final List<T> parameters)
        throws SQLException {
        int index = SQL_PARAMETER_START_INDEX;
        for (T parameter : parameters) {
            preparedStatement.setObject(index, parameter);
            index++;
        }
    }

    private <T> T mapOneResult(final ResultSet resultSet, final RowMapper<T> rowMapper)
        throws SQLException {
        resultSet.next();
        return rowMapper.mapRow(resultSet, 1);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper)
        throws DataAccessException {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return collect(preparedStatement.executeQuery(), rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> collect(final ResultSet resultSet, final RowMapper<T> rowMapper)
        throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, 1));
        }
        return results;
    }
}
