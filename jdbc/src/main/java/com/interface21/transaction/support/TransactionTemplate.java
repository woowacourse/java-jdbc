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

        try { // 트랜잭션 시작 try - catch
            connection.setAutoCommit(false);

            try { // 트랜잭션 보장 try - catch
                callback.doTransaction();
                connection.commit();
            } catch (Exception exceptionWhenCommit) {
                log.error("트랜잭션 실패. 롤백을 시도합니다.", exceptionWhenCommit);

                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("롤백 실패.", ex);
                }

                throw new DataAccessException("비지니스 로직 혹은 커밋 도중 에러가 발생했습니다.", exceptionWhenCommit);
            }

        } catch (SQLException exceptionWhenCommitStart) { // 트랜잭션 실패 시 catch
            throw new DataAccessException("트랜잭션을 시작할 수 없습니다.", exceptionWhenCommitStart);

        } catch (Exception e) { // 트랜잭션 실패는 아니고 그 이외 에러일 때는 rollback 가능
            log.error("예상치 못한 오류. 롤백을 시도합니다.", e);

            try {
                connection.rollback();
            } catch (SQLException ex) {
                log.error("롤백 실패.", ex);
            }

            throw new DataAccessException("예상치 못한 오류가 발생했습니다.", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
