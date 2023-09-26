package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        try(final Connection conn = dataSource.getConnection();
            final PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setAllArguments(args, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setAllArguments(final Object[] args, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }



    public DataSource dataSource() {
        return dataSource;
    }
}
