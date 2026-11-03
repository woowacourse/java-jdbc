package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcTemplate.class);

    public final int update(final User user) {
        final var sql = createQuery();
        log.debug("query : {}", sql);
        try (final var connection = getDataSource().getConnection();
             final var statement = connection.prepareStatement(sql)) {
            setValues(user, statement);
            return statement.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    protected abstract void setValues(User user, PreparedStatement statement) throws SQLException;
}
