package nextstep.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import nextstep.jdbc.RowMapper;

public class FakeRowMapper implements RowMapper {

    @Override
    public Object mapRow(final ResultSet rs) throws SQLException {
        return rs.getString("account");
    }
}
