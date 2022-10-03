package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public <T> List<T> query(String sql, Class<T> clazz, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            setParameters(pstmt, args);
            final ResultSet resultSet = pstmt.executeQuery();

            return ResultDataExtractor.extractData(resultSet, clazz);
        } catch (Exception exception) {
            throw new RuntimeException();
        }
    }

    public <T> T queryForObject(String sql, Class<T> clazz, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            setParameters(pstmt, args);
            final ResultSet resultSet = pstmt.executeQuery();

            return ResultDataExtractor.extractSingleData(resultSet, clazz);
        } catch (Exception exception) {
            throw new RuntimeException();
        }
    }

    private void setParameters(PreparedStatement pstmt, Object[] objects) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            final Class<?> clazz = objects[i].getClass();
            if(clazz == String.class) {
                pstmt.setString(i+1, (String) objects[i]);
            }

            if(clazz == Long.class || clazz == long.class) {
                pstmt.setLong(i+1, (long) objects[i]);
            }
        }
    }

    public void update(String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            setParameters(pstmt, args);

            pstmt.executeUpdate();
        } catch (Exception exception) {
            throw new DataAccessException();
        }
    }
}
