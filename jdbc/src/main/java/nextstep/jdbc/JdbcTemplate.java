package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementSetter pss) {
        execute(sql, pss, PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        List<T> results = query(sql, pss, rowMapper);
        return DataAccessUtils.nullableSingleResult(results);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        PreparedStatementSetter pss = ps -> {};
        return execute(sql, pss, preparedStatement -> getResult(preparedStatement, rowMapper));
    }

    public <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        return execute(sql, pss, preparedStatement -> getResult(preparedStatement, rowMapper));
    }

    private <T> T execute(String sql, PreparedStatementSetter pss, PreparedStatementCallback<T> psc) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            pss.setValues(preparedStatement);
            return psc.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    private <T> List<T> getResult(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> result = new ArrayList<>();
            int rowNum = 0;
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet, rowNum++));
            }
            return result;
        }
    }
}
