package context;

import di.annotation.Controller;
import di.component.ComponentContainer;
import di.component.ComponentScanner;
import di.depdency.DependencyJudge;

import java.util.Map;

public class ApplicationContext {

    private final ComponentContainer componentContainer;

    public ApplicationContext(String... packagePaths) {
        this.componentContainer = new ComponentContainer();
        fillComponentContainer(packagePaths);
    }

    private void fillComponentContainer(String[] packagePaths) {
        ComponentScanner componentScanner = new ComponentScanner(packagePaths);
        DependencyJudge dependencyJudge = new DependencyJudge(componentScanner);
        dependencyJudge.fillComponentContainer(this.componentContainer);
    }

    public Map<Class<?>, Object> findController() {
        return componentContainer.takeComponentsWithAnnotation(Controller.class);
    }

    public Object takeComponent(Class<?> classType) {
        return componentContainer.takeComponent(classType);
    }

    public ComponentContainer getComponentContainer() {
        return componentContainer;
    }
}
