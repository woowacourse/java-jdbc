package nextstep.jdbc;

import javax.annotation.Nullable;
import java.sql.ResultSet;

@FunctionalInterface
public interface RowMapper<T> {

    @Nullable
    T mapRow(ResultSet rs, int rowNum);
}
