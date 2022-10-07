package nextstep.jdbc;

import java.sql.SQLException;

public class UpdateExecutor implements QueryExecutor<Integer> {

    @Override
    public Integer executePreparedStatement(final PreparedStatementStarter pss) throws SQLException {
        return pss.executeUpdate();
    }
}
