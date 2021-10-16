package di.depdency.graph;

import di.depdency.GraphCompositionFailureException;
import di.depdency.ImpossibleDependencyException;
import example.controller.CardController;
import example.repository.CardRepository;
import example.service.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DependencyGraphTest {

    @DisplayName("중복된 연관 관계를 추가하면 예외가 발생한다.")
    @Test
    void connectDuplicatedDependency() {
        // given
        DependencyGraph<Class<?>> dependencyGraph = new DependencyGraph<>(
                Set.of(CardRepository.class, CardService.class)
        );
        dependencyGraph.connect(Set.of(CardRepository.class), CardService.class);

        // when, then
        assertThatThrownBy(() -> dependencyGraph.connect(Set.of(CardRepository.class), CardService.class))
                .isInstanceOf(GraphCompositionFailureException.class)
                .hasMessageContaining("의존성 그래프 구성에 실패했습니다");
    }

    @DisplayName("의존성이 낮은 순으로 위상 정렬한다.")
    @Test
    void orderByDependencyAsc() {
        // given
        DependencyGraph<Class<?>> dependencyGraph = new DependencyGraph<>(
                Set.of(CardController.class, CardRepository.class, CardService.class)
        );
        dependencyGraph.connect(Set.of(CardRepository.class), CardService.class);
        dependencyGraph.connect(Set.of(CardService.class), CardController.class);

        // when
        List<Class<?>> orders = dependencyGraph.orderByDependencyAsc();

        // then
        int repositoryIndex = 0, serviceIndex = 0, controllerIndex = 0;
        for (int i = 0; i < orders.size(); i++) {
            Class<?> aClass = orders.get(i);
            if (aClass == CardRepository.class) {
                repositoryIndex = i;
            }
            if (aClass == CardService.class) {
                serviceIndex = i;
            }
            if (aClass == CardController.class) {
                controllerIndex = i;
            }
        }

        assertThat(repositoryIndex).isLessThan(serviceIndex);
        assertThat(serviceIndex).isLessThan(controllerIndex);
    }

    @DisplayName("의존성 그래프에 사이클이 존재하면 정렬시 예외가 발생한다.")
    @Test
    void orderFailWhenHasCycle() {
        // given
        DependencyGraph<Class<?>> dependencyGraph = new DependencyGraph<>(
                Set.of(CardRepository.class, CardService.class)
        );
        dependencyGraph.connect(Set.of(CardRepository.class), CardService.class);
        dependencyGraph.connect(Set.of(CardService.class), CardRepository.class);

        // when, then
        assertThatThrownBy(() -> dependencyGraph.orderByDependencyAsc())
                .isInstanceOf(ImpossibleDependencyException.class)
                .hasMessageContaining("의존성 그래프에 사이클이 존재합니다");
    }
}
