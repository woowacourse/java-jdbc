package nextstep.jdbc;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;

@FunctionalInterface
public interface PreparedStatementSetter {
    @Nullable
    void setValues(PreparedStatement preparedStatement);
}
