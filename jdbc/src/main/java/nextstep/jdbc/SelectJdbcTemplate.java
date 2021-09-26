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

public abstract class SelectJdbcTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectJdbcTemplate.class);

    public Object query() {
        String sql = createQuery();

        try (Connection connection = getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setValues(preparedStatement);

            List<Object> result = new ArrayList<>();

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                result.add(mapRow(resultSet));
            }

            return result;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Object queryForObject() {
        List<Object> result = (List<Object>) query();

        if (result.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 데이터입니다.");
        }

        if (result.size() > 1) {
            throw new IllegalArgumentException("두 개 이상의 데이터가 존재합니다.");
        }

        return result.get(0);
    }

    public abstract DataSource getDataSource();

    public abstract String createQuery();

    public abstract void setValues(PreparedStatement preparedStatement) throws SQLException;

    public abstract Object mapRow(ResultSet resultSet) throws SQLException;
}
