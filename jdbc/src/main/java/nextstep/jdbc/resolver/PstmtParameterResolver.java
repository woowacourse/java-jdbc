package nextstep.jdbc.resolver;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PstmtParameterResolver {

    void resolve(PreparedStatement pstmt) throws SQLException;
}
