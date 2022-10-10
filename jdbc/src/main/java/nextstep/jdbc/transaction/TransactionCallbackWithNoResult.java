package nextstep.jdbc.transaction;

@FunctionalInterface
public interface TransactionCallbackWithNoResult {

	void execute();
}
