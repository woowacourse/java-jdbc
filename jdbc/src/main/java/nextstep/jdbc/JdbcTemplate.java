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

    private <T, K> T execute(SqlPreProcessor sqlPreProcessor, SqlExecutor<K> sqlExecutor, SqlResultProcessor<T, K> sqlResultProcessor,
                             Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = sqlPreProcessor.preProcess(conn);) {
            setSqlParameters(pstmt, args);
            K sqlResult = sqlExecutor.execute(pstmt);
            return sqlResultProcessor.process(sqlResult);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void update(final String sql, KeyHolder keyHolder, Object... args) {
        SqlPreProcessor sqlPreProcessor = conn -> conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        SqlExecutor<PreparedStatement> sqlExecutor = preparedStatement -> {
            preparedStatement.executeUpdate();
            return preparedStatement;
        };
        SqlResultProcessor<Long, PreparedStatement> sqlResultProcessor = this::getGeneratedKey;

        Long generatedKey = execute(sqlPreProcessor, sqlExecutor, sqlResultProcessor, args);
        KeyHose keyHose = new KeyHose();
        keyHose.injectKey(keyHolder, generatedKey);
    }

    public void update(final String sql, Object... args) {
        SqlPreProcessor sqlPreProcessor = conn -> conn.prepareStatement(sql);
        SqlExecutor<Integer> sqlExecutor = PreparedStatement::executeUpdate;
        SqlResultProcessor<Void, Integer> sqlResultProcessor = sqlResult -> null;
        execute(sqlPreProcessor, sqlExecutor, sqlResultProcessor, args);
    }

    public <T> List<T> finds(ObjectMapper<T> objectMapper, String sql, Object... args) {
        SqlPreProcessor sqlPreProcessor = conn -> conn.prepareStatement(sql);
        SqlExecutor<ResultSet> sqlExecutor = PreparedStatement::executeQuery;
        SqlResultProcessor<List<T>, ResultSet> sqlResultProcessor = sqlResult -> makeObjects(objectMapper, sqlResult);

        return execute(sqlPreProcessor, sqlExecutor, sqlResultProcessor, args);
    }

    public <T> T find(ObjectMapper<T> objectMapper, String sql, Object... args) {
        List<T> results = finds(objectMapper, sql, args);
        if (results.size() != 1) {
            throw new IllegalStateException();
        }
        return results.get(0);
    }

    public boolean deleteAll(final String sql) {
        SqlPreProcessor sqlPreProcessor = conn -> conn.prepareStatement(sql);
        SqlExecutor<Boolean> sqlExecutor = PreparedStatement::execute;
        SqlResultProcessor<Boolean, Boolean> sqlResultProcessor= sqlResult -> sqlResult;
        return execute(sqlPreProcessor, sqlExecutor, sqlResultProcessor);
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

    private <T> List<T> makeObjects(ObjectMapper<T> objectMapper, ResultSet resultSet) throws SQLException {
        List<T> objects = new ArrayList<>();
        while (resultSet.next()) {
            objects.add(objectMapper.mapObject(resultSet));
        }
        return objects;
    }

    private void setSqlParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            pstmt.setObject(index++, arg);
        }
    }
}
