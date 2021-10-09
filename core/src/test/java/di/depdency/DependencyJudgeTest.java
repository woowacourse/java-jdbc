package di.depdency;

import di.component.ComponentContainer;
import di.component.ComponentScanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyJudgeTest {

    @DisplayName("의존성을 판단하여 ComponentContainer 를 채운다.")
    @Test
    void fillComponentContainer() {
        // given
        ComponentScanner componentScanner = new ComponentScanner("example");
        DependencyJudge dependencyJudge = new DependencyJudge(componentScanner);
        ComponentContainer componentContainer = new ComponentContainer();

        // when
        dependencyJudge.fillComponentContainer(componentContainer);

        // then
        Map<Class<?>, Object> components = componentContainer.getComponents();
        assertThat(components).hasSize(12);
        assertThat(components).doesNotContainValue(null);
    }
}
