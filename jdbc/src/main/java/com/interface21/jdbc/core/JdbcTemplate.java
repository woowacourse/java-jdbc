package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeUpdate(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParams(pstmt, params);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> Optional<T> executeQueryWithSingleData(
            String sql,
            Function<ResultSet, T> extractData,
            Object... params
    ) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParams(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            return extractSingle(rs, extractData);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> executeQueryWithMultiData(
            String sql,
            Function<ResultSet, T> extractData,
            Object... params
    ) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParams(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            return extractMultitude(rs, extractData);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    private <T> Optional<T> extractSingle(ResultSet rs, Function<ResultSet, T> extractData) throws SQLException {
        if (rs.next()) {
            return Optional.of(extractData.apply(rs));
        }
        return Optional.empty();
    }

    private <T> List<T> extractMultitude(ResultSet rs, Function<ResultSet, T> extractData) throws SQLException {
        List<T> data = new ArrayList<>();
        while (rs.next()) {
            data.add(extractData.apply(rs));
        }
        return data;
    }
}
