package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(TransactionInterface callback) {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try { // 트랜잭션 보장 try - catch
            connection.setAutoCommit(false);

            callback.doTransaction();

            connection.commit();

        } catch (Exception exceptionWhenCommit) {
            log.error("커밋 실패. 롤백합니다.", exceptionWhenCommit);

            try { // 롤백 try - catch
                connection.rollback();
            } catch (SQLException exceptionWhenRollback) {
                log.error("롤백 실패", exceptionWhenRollback);
            }

            throw new DataAccessException("커밋 중 에러가 발생했습니다. 롤백합니다.", exceptionWhenCommit);

        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
