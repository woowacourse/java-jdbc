package nextstep.jdbc.utils.preparestatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementHelper {

    boolean isAssignable(Object arg);

    void setArgToPsmt(int index, PreparedStatement preparedStatement, Object arg)
        throws SQLException;

    class StringHelper implements PreparedStatementHelper {

        @Override
        public boolean isAssignable(Object arg) {
            return CharSequence.class.isAssignableFrom(arg.getClass());
        }

        @Override
        public void setArgToPsmt(int index, PreparedStatement preparedStatement, Object arg)
            throws SQLException {
            preparedStatement.setString(index, (String) arg);
        }
    }

    class LongHelper implements PreparedStatementHelper {

        @Override
        public boolean isAssignable(Object arg) {
            return Long.class.isAssignableFrom(arg.getClass());
        }

        @Override
        public void setArgToPsmt(int index, PreparedStatement preparedStatement, Object arg)
            throws SQLException {
            preparedStatement.setLong(index, (Long) arg);
        }
    }
}
