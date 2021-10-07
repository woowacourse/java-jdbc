package di.depdency;

import di.annotation.Inject;
import di.depdency.strategy.AnnotationBasedClassInjectStrategy;
import di.depdency.strategy.ConstructorInjectStrategy;
import di.depdency.strategy.FieldInjectStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InjectStrategyRegistry {

    private final List<AnnotationBasedClassInjectStrategy> injectStrategies;

    private InjectStrategyRegistry(AnnotationBasedClassInjectStrategy... injectStrategies) {
        this(Arrays.stream(injectStrategies).collect(Collectors.toList()));
    }

    private InjectStrategyRegistry(List<AnnotationBasedClassInjectStrategy> injectStrategies) {
        this.injectStrategies = injectStrategies;
    }

    public static InjectStrategyRegistry create(AnnotationBasedClassInjectStrategy... injectStrategies) {
        if (injectStrategies.length == 0) {
            return new InjectStrategyRegistry(
                    new FieldInjectStrategy(Inject.class),
                    new ConstructorInjectStrategy(Inject.class)
            );
        }
        return new InjectStrategyRegistry(injectStrategies);
    }

    public AnnotationBasedClassInjectStrategy findProperStrategy(Class<?> aClass) {
        return injectStrategies.stream()
                .filter(injectStrategy -> injectStrategy.supports(aClass))
                .findFirst()
                .orElseThrow(() -> new ImpossibleDependencyException("적절한 의존성 주입 전략을 찾을 수 없습니다."));
    }

    public List<AnnotationBasedClassInjectStrategy> takeAllStrategies() {
        return Collections.unmodifiableList(injectStrategies);
    }
}
