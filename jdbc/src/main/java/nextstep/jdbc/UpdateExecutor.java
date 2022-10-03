package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateExecutor implements QueryExecutor<Integer> {

    @Override
    public Integer executePreparedStatement(final PreparedStatement ps) throws SQLException {
        return ps.executeUpdate();
    }
}
