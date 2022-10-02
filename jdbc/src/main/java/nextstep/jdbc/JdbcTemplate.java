package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.util.DataConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final String SQL_FORMAT_ARGUMENT = "[?]";

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T query(final String sqlFormat,
                       final ResultSetWrapper<T> resultSetWrapper,
                       final Object... sqlArguments) {

        final String sql = generateSql(sqlFormat, sqlArguments);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            log.debug("query : {}", sql);

            if (resultSet.next()) {
                return resultSetWrapper.execute(resultSet);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String generateSql(final String sqlFormat, final Object[] sqlArguments) {
        String sql = sqlFormat;
        for (Object sqlArgument : sqlArguments) {
            final String expectedData = DataConverter.convertObjectToString(sqlArgument);
            sql = sql.replaceFirst(SQL_FORMAT_ARGUMENT, expectedData);
        }
        return sql;
    }
}
