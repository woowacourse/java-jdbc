package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;

public class TransactionManager {

    public static <T> T execute(final Connection conn, final TransactionCallback<T> callback) {
        try {
            begin(conn);
            T result = callback.doInTransaction();
            commit(conn);
            return result;
        } catch (Exception e) {
            rollback(conn);
            throw new DataAccessException("Transaction error", e);
        }    }

    private static void begin(final Connection conn) {
        try {
            conn.setAutoCommit(false);
        } catch (Exception e) {
            throw new DataAccessException("Transaction begin error", e);
        }
    }

    private static void commit(final Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            throw new DataAccessException("Transaction commit error", e);
        }
    }

    private static void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            throw new DataAccessException("Transaction rollback error", e);
        }
    }
}
