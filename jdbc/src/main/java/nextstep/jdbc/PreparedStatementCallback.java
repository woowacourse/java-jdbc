package nextstep.jdbc;

import nextstep.exception.DataAccessException;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    @Nullable
    T doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException;
}
