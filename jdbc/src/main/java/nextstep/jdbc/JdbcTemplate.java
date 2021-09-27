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

public abstract class JdbcTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTemplate.class);

    public void executeQuery(String sql, PreparedStatementSetter preparedStatementSetter) {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            if (preparedStatementSetter != null) {
                preparedStatementSetter.setValues(preparedStatement);
            }

            List<T> result = new ArrayList<>();

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }

            return result;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        List<T> result = query(sql, preparedStatementSetter, rowMapper);

        if (result.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 데이터입니다.");
        }

        if (result.size() > 1) {
            throw new IllegalArgumentException("두 개 이상의 데이터가 존재합니다.");
        }

        return result.get(0);
    }

    public abstract DataSource getDataSource();
}
