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

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            newArgPreparedStatementSetter(args).setValues(pstmt);
            final ResultSet resultSet = pstmt.executeQuery();

            return rowMapper.mapRow(resultSet);
        } catch (SQLException e) {
            throw new DataAccessException("query failed (sql=" + sql + ")", e);
        }
    }

    public void update(final String sql, final Object... args) {
        try (
            final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            newArgPreparedStatementSetter(args).setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("update/insert failed (sql=" + sql + ")", e);
        }
    }

    private PreparedStatementSetter newArgPreparedStatementSetter(final Object[] args) {
        return new ArgumentPreparedStatementSetter(args);
    }
}
