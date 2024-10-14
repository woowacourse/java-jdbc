package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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

    public <T> T queryForObject(ObjectMapper<T> objectMapper, String sql,
                                PreparedStatementSetter preparedStatementSetter) {
        try (Connection conn = getDataSource();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = executeQuery(preparedStatementSetter, pstmt)) {
                if (rs.next()) {
                    return objectMapper.mapToObject(rs);
                }
                throw new DataAccessException("Fail to read result set");
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(ObjectMapper<T> objectMapper, String sql,
                             PreparedStatementSetter preparedStatementSetter) {
        try (Connection conn = getDataSource();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            List<T> results = new ArrayList<>();
            try (ResultSet rs = executeQuery(preparedStatementSetter, pstmt)) {
                while (rs.next()) {
                    results.add(objectMapper.mapToObject(rs));
                }
            }
            return results;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public void update(String sql, PreparedStatementSetter preparedStatementSetter) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // 이미 여기서 닫아버리니 밖에서 롤백을 못함

            preparedStatementSetter.setValues(pstmt);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private Connection getDataSource() throws SQLException {
        return dataSource.getConnection();
    }

    private ResultSet executeQuery(PreparedStatementSetter preparedStatementSetter, PreparedStatement pstmt)
            throws SQLException {
        preparedStatementSetter.setValues(pstmt);
        return pstmt.executeQuery();
    }
}
