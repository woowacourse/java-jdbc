package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

import com.techcourse.domain.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateJdbcTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateJdbcTemplate.class);

    private final DataSource dataSource;

    public UpdateJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(User user) {
        final String sql = createQueryForUpdate();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setValuesForUpdate(user, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String createQueryForUpdate() {
        return "update users set password = ? where id = ?";
    }

    private void setValuesForUpdate(User user, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, user.getPassword());
        preparedStatement.setLong(2, user.getId());
    }

}
