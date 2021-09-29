package nextstep.jdbc.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementSetter {

    void setValues(PreparedStatement preparedStatement) throws SQLException;
}
