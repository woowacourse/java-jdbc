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

    protected static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource datasource;

    public JdbcTemplate(DataSource datasource) {
        this.datasource = datasource;
    }

    public void update(String sql, Object... args) {
        execute(sql, args, PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);

        if (results.size() > 1) {
            throw new DataAccessException("More than one query result");
        }

        return results.stream()
            .findFirst()
            .orElseThrow(() -> new DataAccessException("No Data Found"));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        CallBack<List<T>> execution = pstm -> {
            try (ResultSet rs = pstm.executeQuery()) {
                return ResultSetExtractor.extract(rs, rowMapper);
            }
        };

        return execute(sql, args, execution);
    }

    private <T> T execute(String sql, Object[] args, CallBack<T> sqlExecution) {
        try (
            Connection conn = datasource.getConnection();
            PreparedStatement pstm = conn.prepareStatement(sql);
        ) {
            log.info("query : {}", sql);
            setArguments(pstm, args);
            return sqlExecution.execute(pstm);
        } catch (SQLException e) {
            log.error("SQLException thrown: {}", e.getMessage());
            throw new DataAccessException(e.getMessage());
        }
    }

    private void setArguments(PreparedStatement pstm, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstm.setObject(i + 1, args[i]);
        }
    }
}
