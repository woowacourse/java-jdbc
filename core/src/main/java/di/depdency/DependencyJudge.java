package di.depdency;

import di.component.ComponentContainer;
import di.component.ComponentScanner;
import di.depdency.graph.DependencyGraph;
import di.depdency.strategy.AnnotationBasedClassInjectStrategy;
import di.depdency.strategy.InjectStrategy;
import di.depdency.strategy.MethodInjectStrategy;
import exception.CoreException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DependencyJudge {

    private final ComponentScanner componentScanner;
    private final InjectStrategyRegistry injectStrategyRegistry;
    private final InjectStrategy<Method> methodInjectStrategy;

    public DependencyJudge(ComponentScanner componentScanner) {
        this.componentScanner = componentScanner;
        this.injectStrategyRegistry = InjectStrategyRegistry.create();
        this.methodInjectStrategy = new MethodInjectStrategy();
    }

    public void fillComponentContainer(ComponentContainer componentContainer) {
        DependencyGraph<Class<?>> dependencyGraph = makeDependencyGraph();
        List<Class<?>> orders = dependencyGraph.orderByDependencyAsc();
        Map<Class<?>, Method> methodComponentTables = makeMethodComponentTables();

        for (Class<?> componentType : orders) {
            if (methodComponentTables.containsKey(componentType)) {
                Method method = methodComponentTables.get(componentType);
                registerInstance(componentContainer, method);
            } else {
                AnnotationBasedClassInjectStrategy properStrategy = injectStrategyRegistry.findProperStrategy(componentType);
                properStrategy.registerInstance(componentType, componentContainer);
            }
        }
    }

    private DependencyGraph<Class<?>> makeDependencyGraph() {
        Set<Class<?>> componentClasses = componentScanner.scanComponentClasses();
        DependencyGraph<Class<?>> dependencyGraph = new DependencyGraph<>(makeNodes());
        for (Class<?> componentClass : componentClasses) {
            AnnotationBasedClassInjectStrategy properStrategy = injectStrategyRegistry.findProperStrategy(componentClass);
            Set<Class<?>> dependencies = properStrategy.findDependencies(componentClass);
            dependencyGraph.connect(dependencies, componentClass);
        }

        Set<Method> methods = componentScanner.scanComponentMethodsFromConfiguration();
        for (Method method : methods) {
            if (!methodInjectStrategy.supports(method)) {
                throw new ImpossibleDependencyException("적절한 의존성 주입 전략을 찾을 수 없습니다.");
            }
            Set<Class<?>> dependencies = methodInjectStrategy.findDependencies(method);
            dependencyGraph.connect(dependencies, method.getReturnType());
        }
        return dependencyGraph;
    }

    private Set<Class<?>> makeNodes() {
        Set<Class<?>> nodes = componentScanner.scanComponentClasses();
        nodes.addAll(castReturnTypes(componentScanner.scanComponentMethodsFromConfiguration()));
        return nodes;
    }

    private Set<Class<?>> castReturnTypes(Set<Method> methods) {
        return methods.stream()
                .map(Method::getReturnType)
                .collect(Collectors.toSet());
    }

    private Map<Class<?>, Method> makeMethodComponentTables() {
        HashMap<Class<?>, Method> tables = new HashMap<>();
        Set<Method> methods = componentScanner.scanComponentMethodsFromConfiguration();
        for (Method method : methods) {
            tables.put(method.getReturnType(), method);
        }
        return tables;
    }

    private void registerInstance(ComponentContainer componentContainer, Method method) {
        try {
            methodInjectStrategy.instantiate(method, componentContainer);
        } catch (ReflectiveOperationException e) {
            throw new CoreException("컴포넌트를 생성하는중 문제가 발생했습니다.");
        }

    }
}
