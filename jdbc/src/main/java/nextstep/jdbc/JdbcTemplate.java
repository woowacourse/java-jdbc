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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql);
             final ResultSet resultSet = getResultSet(preparedStatement, params)) {
            return makeQueryResult(rowMapper, resultSet);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> makeQueryResult(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        final List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
        }
        return result;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql);
             final ResultSet resultSet = getResultSet(preparedStatement, params)) {
            checkEmptyResult(resultSet);
            final T result = rowMapper.mapRow(resultSet, resultSet.getRow());
            checkMultiResult(resultSet);
            return result;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void checkEmptyResult(final ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            throw new DataAccessException("결과가 없습니다!");
        }
    }

    private void checkMultiResult(final ResultSet resultSet) throws SQLException {
        if (!resultSet.isAfterLast()) {
            throw new DataAccessException("쿼리 결과가 한 개가 아닙니다.");
        }
    }

    private ResultSet getResultSet(final PreparedStatement prepareStatement, final Object[] params)
            throws SQLException {
        for (int i = 0; i < params.length; i++) {
            prepareStatement.setObject(i - 1, params[i]);
        }

        return prepareStatement.executeQuery();
    }

    public Long insert(final PreparedStatementCreator preparedStatementCreator) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(connection)) {
            preparedStatement.executeUpdate();
            final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public int update(final PreparedStatementCreator preparedStatementCreator) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(connection)) {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
