package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int execute(String sql, Object ... parameters) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ){
            log.debug("query : {}", sql);
            setParameters(parameters, pstmt);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(Object[] parameters, PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setString(i + 1, String.valueOf(parameters[i]));
        }
    }

    public <T> List<T> query(String sql, ResultSetParser<T> resultSetParser, Object ... parameters) {
        ResultSet resultSet = query(sql, parameters);
        try {
            return parseResults(resultSetParser, resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ResultSet query(String sql, Object ... parameters) {
        try{
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            log.debug("query : {}", sql);
            setParameters(parameters, pstmt);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> parseResults(ResultSetParser<T> resultSetParser, ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(resultSetParser.parse(resultSet));
        }
        return results;
    }

    public <T> T queryOne(String sql, ResultSetParser<T> resultSetParser, Object ... parameters) {
        ResultSet resultSet = query(sql, parameters);
        try {
            return parseResults(resultSetParser, resultSet).getFirst();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
