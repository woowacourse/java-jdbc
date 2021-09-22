package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TestUserRowMapper implements RowMapper<TestUser> {

    @Override
    public TestUser map(final ResultSet resultset) throws SQLException {
        return new TestUser(
            resultset.getLong(1),
            resultset.getString(2),
            resultset.getString(3),
            resultset.getString(4)
        );
    }
}
