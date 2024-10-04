package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String query, Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(query);
            setParameters(pstmt, parameters);

            log.debug("query : {}", query);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public <T> T getResult(String query, ObjectMapper<T> objectMapper, Object... parameters) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(query);
            setParameters(pstmt, parameters);

            log.info("query : {}", query);

            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return objectMapper.map(resultSet);
            }
            throw new NoSuchElementException("\"%s\" 에 해당하는 결과가 존재하지 않습니다.".formatted(query));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public <T> List<T> getResults(String query, ObjectMapper<T> objectMapper, Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(query);
            setParameters(pstmt, parameters);

            log.info("query : {}", query);

            ResultSet resultSet = pstmt.executeQuery();
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(objectMapper.map(resultSet));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private void setParameters(PreparedStatement pstmt, Object... parameters) throws SQLException {
        for (int i = 1; i <= parameters.length; i++) {
            setParameter(pstmt, parameters[i-1], i);
        }
    }
    private void setParameter(PreparedStatement pstmt, Object parameter, int index) throws SQLException {
        Class<?> parameterClass = parameter.getClass();
        if (parameterClass.equals(String.class)) {
            pstmt.setString(index, (String) parameter);
        }
        if (parameterClass.equals(Long.class)) {
            pstmt.setLong(index, (long) parameter);
        }
    }
}
