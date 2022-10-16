package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private Object[] args;

    public ArgumentPreparedStatementSetter(Object... args) {
        this.args = args;
    }

    @Override
    public void setValue(PreparedStatement pstmt) throws SQLException {
        int index = 0;
        for (Object arg : args) {
            index++;
            switch (arg.getClass().getName()) {
                case "String":
                    pstmt.setString(index, (String)arg);
                    break;
                case "Long":
                    pstmt.setLong(index, (Long)arg);
                    break;
                default:
                    pstmt.setObject(index, arg);
            }
        }
    }
}
