package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {
    protected static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    protected DataSource datasource;

    public JdbcTemplate(DataSource datasource) {
        this.datasource = datasource;
    }

    public void update(String sql, Object... args) {
        execute(sql, args, PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, rowMapper, args);
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        CallBack<T> execution = pstm -> {
            ResultSet rs = pstm.executeQuery();
            return rowMapper.mapRow(rs);
        };

        return execute(sql, args, execution);
    }

    private <T> T execute(String sql, Object[] args, CallBack<T> sqlExecution) {
        try (
            Connection conn = datasource.getConnection();
            PreparedStatement pstm = conn.prepareStatement(sql);
        ) {
            log.info("query : {}", sql);
            for (int i = 0; i < args.length; i++) {
                pstm.setObject(i + 1, args[i]);
            }
            return sqlExecution.execute(pstm);
        } catch (SQLException e) {
            log.error("SQLException thrown: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
