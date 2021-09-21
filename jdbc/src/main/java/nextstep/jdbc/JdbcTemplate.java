package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.exception.JdbcNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final JdbcResources jdbcResources;

    public JdbcTemplate(final DataSource dataSource) {
        this.jdbcResources = new JdbcResources(dataSource);
    }

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        try {
            log.debug("query : {}", sql);
            ResultSet resultSet = jdbcResources.getResultSet(sql, arguments);

            if (!resultSet.next()) {
                throw new JdbcNotFoundException(String.format("조건을 만족하는 행을 찾지 못했습니다.\nsql:(%s)\n", sql));
            }
            return rowMapper.map(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            jdbcResources.closeAll();
        }
    }
}
