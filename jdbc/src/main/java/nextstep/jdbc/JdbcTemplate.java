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

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Object queryForObject(String query, RowMapper rowMapper, Object... args) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(query);

        try (connection; statement) {
            int index = 1;
            for (Object arg : args) {
                statement.setObject(index++, arg);
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return rowMapper.map(resultSet, resultSet.getRow());
            }
        }
        throw new SQLException();
    }
}
