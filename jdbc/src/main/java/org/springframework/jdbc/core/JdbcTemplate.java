package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void execute(final String sql, final Object... parameters) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            int i = 1;
            for (Object parameter : parameters) {
                pstmt.setObject(i, parameter);
                i++;
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql);
                final ResultSet resultSet = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);

            final List<T> users = new ArrayList<>();

            while (resultSet.next()) {
                T result = rowMapper.rowMap(resultSet);
                users.add(result);
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        ResultSet resultSet = null;
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            int i = 1;
            for (Object parameter : parameters) {
                pstmt.setObject(i, parameter);
                i++;
            }
            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                return rowMapper.rowMap(resultSet);
            }
            throw new NoSuchElementException();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (Objects.nonNull(resultSet)) {
                    resultSet.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public int update(final String sql, final Object... parameters) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            int i = 1;
            for (Object parameter : parameters) {
                pstmt.setObject(i, parameter);
                i++;
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }
}
