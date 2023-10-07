package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementSetter {

    private final Object[] objects;

    public PreparedStatementSetter(Object... objects) {
        this.objects = objects;
    }

    public void setObjects(PreparedStatement preparedStatement) throws SQLException {
        for (int index = 0; index < objects.length; index++) {
            preparedStatement.setString(index + 1, String.valueOf(objects[index]));
        }
    }

}
