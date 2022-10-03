package nextstep.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.PreparedStatementSetter;

public class FakePreparedStatementSetter implements PreparedStatementSetter {

    private final String sql = "insert into users (account, password, email) values (?, ?, ?)";

    @Override
    public PreparedStatement setPreparedStatement(final Connection connection) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, "dummy");
        pstmt.setString(2, "dummyPassword");
        pstmt.setString(3, "dummy@aaaa.com");
        return pstmt;
    }
}
