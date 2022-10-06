package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public Long insert(final String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setSqlParameters(pstmt, args);
            pstmt.executeUpdate();
            return getGeneratedKey(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Long getGeneratedKey(PreparedStatement pstmt) {
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getLong("id");
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void update(final String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setSqlParameters(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> finds(ObjectMapper<T> objectMapper, String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setSqlParameters(pstmt, args);
            return executeSqlAndMakeObjects(objectMapper, pstmt);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private <T> List<T> executeSqlAndMakeObjects(ObjectMapper<T> objectMapper, PreparedStatement pstmt) {
        try (ResultSet resultSet = pstmt.executeQuery()) {
            return makeObjects(objectMapper, resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private <T> List<T> makeObjects(ObjectMapper<T> objectMapper, ResultSet resultSet) throws SQLException {
        List<T> objects = new ArrayList<>();
        while (resultSet.next()) {
            objects.add(objectMapper.mapObject(resultSet));
        }
        return objects;
    }

    public <T> T find(ObjectMapper<T> objectMapper, String sql, Object... args) {
        List<T> results = finds(objectMapper, sql, args);
        if (results.size() != 1) {
            throw new IllegalStateException();
        }
        return results.get(0);
    }

    public void deleteAll(final String sql) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void setSqlParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            pstmt.setObject(index++, arg);
        }
    }
}
