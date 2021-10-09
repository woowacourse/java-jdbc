package di.depdency.strategy;

import di.annotation.Component;
import di.component.ComponentContainer;
import di.depdency.ImpossibleDependencyException;
import example.configuration.DummyDataSource;
import example.configuration.TestConfiguration;
import example.errorcase.DuplicationParametersMethod;
import example.repository.CardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MethodInjectStrategyTest {

    @DisplayName("메서드 주입 전략을 사용할 수 있는지 검사한다.")
    @Test
    void supports() {
        MethodInjectStrategy methodInjectStrategy = new MethodInjectStrategy();
        Set<Method> methods = ReflectionUtils.getMethods(
                TestConfiguration.class, ReflectionUtils.withAnnotation(Component.class)
        );
        Method method = extractFirstMethod(methods);

        assertThat(methodInjectStrategy.supports(method)).isTrue();
    }


    @DisplayName("메서드 내에 같은 타입의 파라미터가 있는 경우 예외가 발생한다.")
    @Test
    void notSupportsWithDuplicateParameterTypes() {
        MethodInjectStrategy methodInjectStrategy = new MethodInjectStrategy();
        Set<Method> methods = ReflectionUtils.getMethods(DuplicationParametersMethod.class);
        Method method = extractFirstMethod(methods);

        assertThatThrownBy(() -> methodInjectStrategy.supports(method))
                .isInstanceOf(ImpossibleDependencyException.class)
                .hasMessageContaining("메서드에 중복 필드가 있습니다");
    }

    @DisplayName("의존성을 가지고 있는 클래스를 찾는다.")
    @Test
    void findDependencies() {
        MethodInjectStrategy methodInjectStrategy = new MethodInjectStrategy();
        Set<Method> methods = ReflectionUtils.getMethods(
                TestConfiguration.class, ReflectionUtils.withAnnotation(Component.class)
        );
        Method method = extractFirstMethod(methods);

        assertThat(methodInjectStrategy.findDependencies(method)).contains(TestConfiguration.class, CardRepository.class);
    }

    @DisplayName("객체를 생성하여 ComponentContainer에 등록한다.")
    @Test
    void instantiate() throws ReflectiveOperationException {
        // given
        ComponentContainer componentContainer = new ComponentContainer();
        CardRepository cardRepository = new CardRepository();
        TestConfiguration testConfiguration = new TestConfiguration();
        componentContainer.register(CardRepository.class, cardRepository);
        componentContainer.register(TestConfiguration.class, testConfiguration);

        MethodInjectStrategy methodInjectStrategy = new MethodInjectStrategy();
        Set<Method> methods = ReflectionUtils.getMethods(
                TestConfiguration.class, ReflectionUtils.withAnnotation(Component.class)
        );
        Method method = extractFirstMethod(methods);

        // when
        methodInjectStrategy.instantiate(method, componentContainer);

        // then
        Object object = componentContainer.takeComponent(DummyDataSource.class);
        assertThat(object).isInstanceOf(DummyDataSource.class);
        assertThat(object).isNotNull();
    }

    private Method extractFirstMethod(Set<Method> methods) {
        return methods.stream().findFirst().get();
    }
}
