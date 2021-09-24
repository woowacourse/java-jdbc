package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                preparedStatement.setObject(i, args[i - 1]);
            }
            return preparedStatement;
        }, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet, 0);
            }
            return null;
        });
    }

    public int update(PreparedStatementCreator preparedStatementCreator) {
        return execute(preparedStatementCreator, ps -> {
            int rows = ps.executeUpdate();
            return rows;
        });
    }

    private <T> T execute(PreparedStatementCreator preparedStatementCreator, PreparedStatementCallback<T> action) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(connection)) {

            T result = action.doInPreparedStatement(preparedStatement);
            return result;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            throw new RuntimeException();
        }
    }
}
