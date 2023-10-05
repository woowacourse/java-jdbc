package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            throw new EmptyResultAccessException();
        }
        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            PreparedStatementSetter pstSetter = new ArgumentPreparedStatementSetter(args);
            pstSetter.setValues(pstmt);

            ResultSet resultSet = pstmt.executeQuery();
            RowMapperResultsetExtractor<T> rowMapperResultsetExtractor = new RowMapperResultsetExtractor<>(rowMapper);
            return rowMapperResultsetExtractor.extractData(resultSet);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void update(final String sql, final Object... args) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            PreparedStatementSetter pstSetter = new ArgumentPreparedStatementSetter(args);
            pstSetter.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
