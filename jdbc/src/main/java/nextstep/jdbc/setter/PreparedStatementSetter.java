package nextstep.jdbc.setter;

import java.sql.PreparedStatement;

public interface PreparedStatementSetter {

    void setValues(PreparedStatement preparedStatement);
}
