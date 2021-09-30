package nextstep.jdbc;

import javax.annotation.Nullable;
import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetExtractor<T> {

    @Nullable
    T extractData(ResultSet rs);
}
