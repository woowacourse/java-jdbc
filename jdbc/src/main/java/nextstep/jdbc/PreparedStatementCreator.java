package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.IntStream;

public class PreparedStatementCreator {

    private final String sql;
    private final Object[] objects;

    public PreparedStatementCreator(final String sql, final Object[] objects) {
        this.sql = sql;
        if (objects == null) {
            this.objects = new Object[]{};
            return;
        }
        this.objects = objects;
    }

    public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        IntStream.range(0, objects.length)
                .forEach(IntConsumerWrapper.accept(index -> pstmt.setObject(index + 1, objects[index])));

        return pstmt;
    }
}
