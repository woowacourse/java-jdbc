package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class JdbcTemplate {
    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update() {
        // todo
    }

//    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
//        // todo
//        return query(sql, rowMapper, args);
//    }
//
//    private <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
//        ResultSet resultSet = execute(sql, args);
//        // todo
//    }

    private ResultSet execute(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstm = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                StatementCreatorUtils.setParameterValue(pstm, i, args[i]);
            }
            return pstm.executeQuery();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
