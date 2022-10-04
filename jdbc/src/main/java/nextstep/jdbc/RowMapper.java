package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface RowMapper<T> {

    @Nullable
    T mapRow(final ResultSet rs) throws SQLException;
}
