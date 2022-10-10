package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] objects;

    public ArgumentPreparedStatementSetter(final Object[] objects) {
        this.objects = objects;
    }

    @Override
    public void setValues(final PreparedStatement ps) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            ps.setObject(i + 1, objects[i]);
        }
    }
}
