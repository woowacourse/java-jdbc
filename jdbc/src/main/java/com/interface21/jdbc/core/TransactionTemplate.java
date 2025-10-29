package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionCallBack<T> action) {
        Connection existing = TransactionSynchronizationManager.getResource(dataSource);
        if (existing != null) {
            return action.execute();
        }

        Connection con = null;
        boolean begunByMe = false;
        boolean prevAutoCommit = true;

        try {
            con = dataSource.getConnection();
            prevAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            TransactionSynchronizationManager.bindResource(dataSource, con);
            begunByMe = true;

            T result = action.execute();
            con.commit();
            return result;

        } catch (RuntimeException | Error e) {
            safeRollback(con);
            throw e;
        } catch (Exception e) {
            safeRollback(con);
            throw new DataAccessException(e);
        } finally {
            if (begunByMe) {
                try {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                } catch (Exception ignore) {
                }
                safeSetAutoCommit(con, prevAutoCommit);   // ★ 이전값으로 복원
                safeClose(con);
            }
        }
    }

    private void safeRollback(Connection c) {
        if (c != null) {
            try {
                c.rollback();
            } catch (Exception ignore) {
            }
        }
    }

    private void safeSetAutoCommit(Connection c, boolean v) {
        if (c != null) {
            try {
                c.setAutoCommit(v);
            } catch (Exception ignore) {
            }
        }
    }

    private void safeClose(Connection c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception ignore) {
            }
        }
    }
}
