package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;

public class SingleColumnRowMapper<T> implements RowMapper<T> {

    private Class<?> requiredType;

    public SingleColumnRowMapper(final Class<T> requiredType) {
        this.requiredType = PrimitiveClassUtils.wrapPrimitiveClassIfNecessary(requiredType);
    }

    @Override
    public T mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();
        if (nrOfColumns != 1) {
            throw new IncorrectResultSetColumnCountException(1, nrOfColumns);
        }
        return (T) rs.getObject(1);
    }
}
