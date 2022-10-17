package nextstep.jdbc.transaction;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionTemplate {

	private final PlatformTransactionManager transactionManager;

	public TransactionTemplate(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void doInTransactionWithNoResult(TransactionCallbackWithNoResult callback) {
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			callback.execute();
			transactionManager.commit(status);
		} catch (Exception e) {
			transactionManager.rollback(status);
			throw e;
		}
	}


	public <T> T doInTransaction(TransactionCallback<T> callback) {
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			T result = callback.execute();
			transactionManager.commit(status);
			return result;
		} catch (Exception e) {
			transactionManager.rollback(status);
			throw e;
		}
	}
}
