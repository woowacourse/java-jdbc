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

    public int update(final String sql, final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }

            return preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
            // TODO: 더 추상화된 예외 메시지를 사용해야함
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }

            List<T> results = new ArrayList<>();
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                T result = rowMapper.run(rs);
                results.add(result);
            }

            return results;
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
            // TODO: 더 추상화된 예외 메시지를 사용해야함
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }

            T result = null;
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                result = rowMapper.run(rs);
            }

            return result;
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
            // TODO: 더 추상화된 예외 메시지를 사용해야함
        }
    }
}
