package nextstep.transaction;

import java.sql.Connection;
import nextstep.transaction.support.TransactionIsolation;
import nextstep.transaction.support.TransactionPropagation;

public class TransactionStatus {

    private TransactionIsolation transactionIsolation;
    private TransactionPropagation transactionPropagation;
    private boolean readOnly;
    private Connection connection;

    public TransactionStatus(final TransactionIsolation transactionIsolation,
                             final TransactionPropagation transactionPropagation,
                             final boolean readOnly,
                             final Connection connection) {
        this.transactionIsolation = transactionIsolation;
        this.transactionPropagation = transactionPropagation;
        this.readOnly = readOnly;
        this.connection = connection;
    }

    public TransactionIsolation getTransactionIsolation() {
        return transactionIsolation;
    }

    public TransactionPropagation getTransactionPropagation() {
        return transactionPropagation;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public Connection getConnection() {
        return connection;
    }
}
