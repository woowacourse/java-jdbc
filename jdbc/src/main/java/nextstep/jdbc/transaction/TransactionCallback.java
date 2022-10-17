package nextstep.jdbc.transaction;

@FunctionalInterface
public interface TransactionCallback <T> {

	T execute();
}
