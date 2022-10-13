package nextstep.jdbc;

import java.sql.PreparedStatement;

public class EmptyPreparedStatement implements PreparedStatementSetter{

    @Override
    public void setValues(PreparedStatement preparedStatement) {
    }
}
