package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int execute(String sql, Object... parameters) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            setParameters(parameters, pstmt);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParameters(Object[] parameters, PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setString(i + 1, String.valueOf(parameters[i]));
        }
    }

    public <T> List<T> query(String sql, ResultSetParser<T> resultSetParser, Object... parameters) {
        ResultSet resultSet = query(sql, parameters);
        try {
            return parseResults(resultSetParser, resultSet);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private ResultSet query(String sql, Object... parameters) {
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            log.debug("query : {}", sql);
            setParameters(parameters, pstmt);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> parseResults(ResultSetParser<T> resultSetParser, ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(resultSetParser.parse(resultSet));
        }
        return results;
    }

    public <T> T queryOne(String sql, ResultSetParser<T> resultSetParser, Object... parameters) {
        ResultSet resultSet = query(sql, parameters);
        try {
            return parseResult(resultSetParser, resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T parseResult(ResultSetParser<T> resultSetParser, ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            throw new DataAccessException("행이 하나도 조회되지 않았습니다.");
        }
        if (!resultSet.isLast()) {
            throw new DataAccessException("여러개의 행이 조회되었습니다.");
        }
        return resultSetParser.parse(resultSet);
    }
}
