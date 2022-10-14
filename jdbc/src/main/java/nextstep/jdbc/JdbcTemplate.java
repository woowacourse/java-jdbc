package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(SqlPreProcessor sqlPreProcessor, SqlExecutor<T> sqlExecutor, String sql,
                          Object... args) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = sqlPreProcessor.preProcess(conn)) {
            setSqlParameters(pstmt, args);
            return sqlExecutor.execute(pstmt);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new JdbcExecuteException("sql \"" + sql + "\" exception!");
        }
    }

    public void update(final String sql, KeyHolder keyHolder, Object... args) {
        SqlPreProcessor sqlPreProcessor = conn -> conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        SqlExecutor<Long> sqlExecutor = preparedStatement -> {
            preparedStatement.executeUpdate();
            return getGeneratedKey(preparedStatement);
        };

        Long generatedKey = execute(sqlPreProcessor, sqlExecutor, sql, args);
        KeyHose keyHose = new KeyHose();
        keyHose.injectKey(keyHolder, generatedKey);
    }

    public void update(final String sql, Object... args) {
        SqlPreProcessor sqlPreProcessor = conn -> conn.prepareStatement(sql);
        SqlExecutor<Integer> sqlExecutor = PreparedStatement::executeUpdate;
        execute(sqlPreProcessor, sqlExecutor, sql, args);
    }

    public <T> List<T> query(RowMapper<T> rowMapper, String sql, Object... args) {
        ObjectFactory<T> objectFactory = new ObjectFactory<>(rowMapper);
        SqlPreProcessor sqlPreProcessor = conn -> conn.prepareStatement(sql);
        SqlExecutor<List<T>> sqlExecutor = preparedStatement -> objectFactory.build(preparedStatement.executeQuery());

        return execute(sqlPreProcessor, sqlExecutor, sql, args);
    }

    public <T> T queryForObject(RowMapper<T> rowMapper, String sql, Object... args) {
        List<T> results = query(rowMapper, sql, args);
        if (results.size() != 1) {
            throw new IllegalStateException("object size is not one");
        }
        return results.get(0);
    }

    private Long getGeneratedKey(PreparedStatement pstmt) {
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            rs.next();
            return rs.getLong("id");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("failed to get generatedKey");
        }
    }

    private void setSqlParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            pstmt.setObject(index++, arg);
        }
    }
}
