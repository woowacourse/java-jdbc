package nextstep.jdbc.transaction;

@FunctionalInterface
public interface TransactionConsumer {
    void consume() throws Exception;
}
