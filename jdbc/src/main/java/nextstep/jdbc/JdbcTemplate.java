package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.util.SqlArgumentConverter;
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
        final List<T> results = queryForList(sqlFormat, resultSetWrapper, sqlArguments);
        assert results != null;
        return results.get(0);
    }

    public <T> List<T> queryForList(final String sqlFormat,
                                    final ResultSetWrapper<T> resultSetWrapper,
                                    final Object... sqlArguments) {
        final String sql = generateSql(sqlFormat, sqlArguments);
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            log.debug("query : {}", sql);
            List<T> results = new ArrayList<>();

            while (resultSet.next()) {
                results.add(resultSetWrapper.execute(resultSet));
            }

            if (results.isEmpty()) {
                return null;
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void update(final String sqlFormat, final Object... sqlArguments) {
        String sql = generateSql(sqlFormat, sqlArguments);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String generateSql(final String sqlFormat, final Object[] sqlArguments) {
        String sql = sqlFormat;
        for (Object sqlArgument : sqlArguments) {
            final String expectedData = SqlArgumentConverter.convertObjectToString(sqlArgument);
            sql = sql.replaceFirst(SQL_FORMAT_ARGUMENT, expectedData);
        }
        return sql;
    }
}
