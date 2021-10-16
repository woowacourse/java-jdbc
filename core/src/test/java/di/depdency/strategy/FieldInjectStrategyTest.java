package di.depdency.strategy;

import di.annotation.Inject;
import di.component.ComponentContainer;
import di.depdency.ImpossibleDependencyException;
import example.errorcase.DuplicateFieldObject;
import example.errorcase.NoDefaultConstructor;
import example.repository.CardRepository;
import example.service.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldInjectStrategyTest {

    @DisplayName("클래스가 필드 주입 전략을 사용할 수 있는지 검사한다.")
    @Test
    void supports() {
        FieldInjectStrategy fieldInjectStrategy = new FieldInjectStrategy(Inject.class);
        assertThat(fieldInjectStrategy.supports(CardService.class)).isTrue();
    }

    @DisplayName("클래스내의 같은 타입의 필드에 @Inject 가 붙어있는 경우 예외가 발생한다.")
    @Test
    void notSupportsWithDuplicateField() {
        FieldInjectStrategy fieldInjectStrategy = new FieldInjectStrategy(Inject.class);
        assertThatThrownBy(() -> fieldInjectStrategy.supports(DuplicateFieldObject.class))
                .isInstanceOf(ImpossibleDependencyException.class)
                .hasMessageContaining("@Inject가 붙어있는 중복 필드가 있습니다");
    }

    @DisplayName("필드 주입 전략을 사용할 때 기본 생성자가 없으면 예외가 발생한다.")
    @Test
    void notSupportsWithNoDefaultConstructor() {
        FieldInjectStrategy fieldInjectStrategy = new FieldInjectStrategy(Inject.class);
        assertThatThrownBy(() -> fieldInjectStrategy.supports(NoDefaultConstructor.class))
                .isInstanceOf(ImpossibleDependencyException.class)
                .hasMessageContaining("필드 주입 전략은 기본 생성자가 필요합니다");
    }

    @DisplayName("의존성을 가지고 있는 클래스를 찾는다.")
    @Test
    void findDependencies() {
        FieldInjectStrategy fieldInjectStrategy = new FieldInjectStrategy(Inject.class);
        Set<Class<?>> dependencies = fieldInjectStrategy.findDependencies(CardService.class);
        assertThat(dependencies).containsExactly(CardRepository.class);
    }

    @DisplayName("객체를 생성하여 ComponentContainer에 등록한다.")
    @Test
    void instantiate() throws ReflectiveOperationException {
        // given
        ComponentContainer componentContainer = new ComponentContainer();
        CardRepository cardRepository = new CardRepository();
        componentContainer.register(CardRepository.class, cardRepository);
        FieldInjectStrategy fieldInjectStrategy = new FieldInjectStrategy(Inject.class);

        // when
        fieldInjectStrategy.instantiate(CardService.class, componentContainer);

        // then
        Object object = componentContainer.takeComponent(CardService.class);
        assertThat(object).isInstanceOf(CardService.class);
        assertThat(object).isNotNull();
        assertThat(((CardService) object).getCardRepository()).isSameAs(cardRepository);
    }
}
