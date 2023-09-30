package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... values) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);

            setValues(pstmt, values);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... values) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);

            setValues(pstmt, values);
            ResultSet resultSet = pstmt.executeQuery();

            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.run(resultSet));
            }

            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... values) {
        List<T> result = query(sql, rowMapper, values);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    public void execute(String sql) {
        try (
                Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement();
        ) {
            log.debug("query : {}", sql);

            stmt.execute(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setValues(PreparedStatement pstmt, Object[] values) throws SQLException {
        for (int i = 1; i <= values.length; i++) {
            pstmt.setObject(i, values[i - 1]);
        }
    }
}
