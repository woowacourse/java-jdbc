package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementSetterImpl implements PreparedStatementSetter{

    private final Object[] parameters;

    public PreparedStatementSetterImpl(Object ... parameters) {
        this.parameters = parameters;
    }


    @Override
    public void setValues(PreparedStatement pstmt) throws SQLException {
        for (int i=0;i<parameters.length;i++) {
            pstmt.setObject(i+1, parameters[i]);
        }
    }
}
