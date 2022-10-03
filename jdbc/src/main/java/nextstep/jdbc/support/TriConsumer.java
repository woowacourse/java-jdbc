package nextstep.jdbc.support;

@FunctionalInterface
public interface TriConsumer<T, U, R> {

    void apply(T t, U u, R r);
}
