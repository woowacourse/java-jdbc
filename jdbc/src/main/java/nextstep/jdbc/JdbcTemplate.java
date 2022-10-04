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

    public void execute(String sql, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            setParams(preparedStatement, params);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        try {
            List<T> result = query(sql, rowMapper, params);
            return extractObject(result);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T extractObject(List<T> result) throws SQLException {
        if (result.size() != 1) {
            throw new IncorrectResultSizeDataAccessException();
        }
        return result.get(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            List<T> objects = new ArrayList<>();
            setParams(preparedStatement, params);
            ResultSet resultSet = preparedStatement.executeQuery();
            for (int rowNumber = 0; resultSet.next(); rowNumber++) {
                objects.add(rowMapper.mapRow(resultSet, rowNumber));
            }
            return objects;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(PreparedStatement preparedStatement, Object... params) throws SQLException {
        int index = 1;
        for (Object param : params) {
            preparedStatement.setObject(index++, param);
        }
    }
}

