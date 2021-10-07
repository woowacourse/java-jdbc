package di.depdency.strategy;

import java.util.Set;

public interface InjectStrategy<T> {

    boolean supports(T type);

    Set<T> findDependencies(T type);
}
