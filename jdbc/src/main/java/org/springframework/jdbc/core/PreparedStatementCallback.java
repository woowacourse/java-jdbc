package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nullable;
import org.springframework.dao.DataAccessException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    @Nullable
    T doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException;
}
