package com.interface21.jdbc.core;

import com.interface21.jdbc.util.TestObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.SQLException;

class StatementSetterTest {

    PreparedStatement statement = Mockito.spy(PreparedStatement.class);

    @Test
    void string_execute_with_setString() throws SQLException {
        final String str = "this is string";
        StatementSetter.setStatementsWithPOJOType(statement, str);
        Mockito.verify(statement, Mockito.times(1))
                .setString(1, str);
    }

    @Test
    void long_execute_with_setLong() throws SQLException {
        final long l = 1L;
        StatementSetter.setStatementsWithPOJOType(statement, l);
        Mockito.verify(statement, Mockito.times(1))
                .setLong(1, l);
    }

    @Test
    void object_execute_with_setObject() throws SQLException {
        final TestObject object = new TestObject("Hi Livi!");
        StatementSetter.setStatementsWithPOJOType(statement, object);
        Mockito.verify(statement, Mockito.times(1)).setObject(1, object);
    }
}
