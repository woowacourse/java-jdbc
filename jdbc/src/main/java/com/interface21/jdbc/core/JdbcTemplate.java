package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ResultSet execute(String sql, String... parameters) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
        ) {
            resolvePrepareStatement(pstmt, parameters);
            return pstmt.executeQuery();
        } catch (Exception exception) {
            throw new DataAccessException(exception);
        }
    }

    private void resolvePrepareStatement(PreparedStatement pstmt, String... parameters) {
        try {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setString(i + 1, parameters[i]);
            }
        } catch (SQLException sqlException) {
            throw new DataAccessException("sql 구성 과정에서 문제가 발생했습니다.", sqlException);
        }
    }
}
