package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    @Nullable
    T doInStatement(PreparedStatement stmt) throws SQLException;
}
