package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplateException.SqlException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        log.debug("query : {}", sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preparedStatementSetter.set(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new SqlException(e.getMessage());
        }
    }

    public <T> T find(final String sql,
                      final PreparedStatementSetter preparedStatementSetter,
                      final ResultSetGetter<T> rsg) {
        log.debug("query : {}", sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preparedStatementSetter.set(ps);
            return getObject(rsg, ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new SqlException(e.getMessage());
        }
    }

    private <T> T getObject(ResultSetGetter<T> rsg, PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rsg.getObject(rs);
            }
            return null;
        }
    }

    public <T> List<T> findAll(final String sql,
                      final ResultSetGetter<T> rsg) {
        log.debug("query : {}", sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return getObjects(rsg, ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new SqlException(e.getMessage());
        }
    }

    private <T> List<T> getObjects(ResultSetGetter<T> rsg, PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<T> objects = new ArrayList<>();
            while (rs.next()) {
                objects.add(rsg.getObject(rs));
            }
            return objects;
        }
    }
}
