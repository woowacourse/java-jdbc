package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.xml.crypto.Data;

import com.techcourse.domain.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InsertJdbcTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertJdbcTemplate.class);

    private final DataSource dataSource;

    public InsertJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        final String sql = createQueryForInsert();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setValuesForInsert(user, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String createQueryForInsert() {
        return "insert into users (account, password, email) values (?, ?, ?)";
    }

    private void setValuesForInsert(User user, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, user.getAccount());
        preparedStatement.setString(2, user.getPassword());
        preparedStatement.setString(3, user.getEmail());
    }
}
