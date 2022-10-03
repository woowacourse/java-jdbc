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

    private static final int PARAMETER_START_INDEX = 1;

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
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return rowMapper.mapRow(resultSet, 1);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T, K> T queryForObject(final String sql, final List<K> parameters,
                                final RowMapper<T> rowMapper) throws DataAccessException {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameters(preparedStatement, parameters);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return rowMapper.mapRow(resultSet, 1);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> void setParameters(PreparedStatement preparedStatement, List<T> parameters)
        throws SQLException {
        int index = PARAMETER_START_INDEX;
        for (T parameter : parameters) {
            preparedStatement.setObject(index, parameter);
            index++;
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper)
        throws DataAccessException {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return collect(preparedStatement.executeQuery(), rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> collect(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, 1));
        }
        return results;
    }
}
