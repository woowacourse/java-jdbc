package nextstep.jdbc.utils;

import java.sql.SQLException;

public class TransactionManager {

    public static void startTransaction() {
        ConnectionUtils.startTransaction();
    }

    public static void endTransaction() {
        ConnectionUtils.endTransaction();
    }

    public static void rollback() {
        try {
            ConnectionUtils.getConnection().rollback();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }
}
