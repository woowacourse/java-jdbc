package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.techcourse.domain.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTemplate.class);

    public void update(User user) {
        String sql = createQuery();

        try (Connection connection = getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setValues(user, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public abstract DataSource getDataSource();

    public abstract String createQuery();

    public abstract void setValues(User user, PreparedStatement preparedStatement) throws SQLException;
}
