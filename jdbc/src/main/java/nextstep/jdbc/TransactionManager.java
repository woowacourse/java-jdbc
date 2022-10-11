package nextstep.jdbc;

import javax.sql.DataSource;
import org.springframework.transaction.TransactionException;

public interface TransactionManager {

    void getTransaction(final DataSource dataSource) throws TransactionException;


    void commit();

    void rollback();
}
