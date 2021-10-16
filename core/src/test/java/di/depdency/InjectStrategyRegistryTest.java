package di.depdency;

import di.depdency.strategy.AnnotationBasedClassInjectStrategy;
import di.depdency.strategy.FieldInjectStrategy;
import example.service.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InjectStrategyRegistryTest {

    @DisplayName("create 팩터리 메서드에 파라미터가 없으면 기본 주입 전략으로 생성된다.")
    @Test
    void createWithDefaultStrategies() {
        InjectStrategyRegistry injectStrategyRegistry = InjectStrategyRegistry.create();

        assertThat(injectStrategyRegistry.takeAllStrategies()).hasSize(2);
    }

    @DisplayName("적절한 생성자 주입 전략을 찾는다.")
    @Test
    void findProperStrategy() {
        InjectStrategyRegistry injectStrategyRegistry = InjectStrategyRegistry.create();

        AnnotationBasedClassInjectStrategy properStrategy = injectStrategyRegistry.findProperStrategy(CardService.class);

        assertThat(properStrategy).isInstanceOf(FieldInjectStrategy.class);
    }
}
