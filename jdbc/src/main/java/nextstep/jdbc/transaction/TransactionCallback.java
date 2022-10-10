package nextstep.jdbc.transaction;

@FunctionalInterface
public interface TransactionCallback {

	void execute();
}
