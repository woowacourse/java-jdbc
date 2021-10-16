package di.depdency.strategy;

import di.annotation.Inject;
import di.component.ComponentContainer;
import di.depdency.ImpossibleDependencyException;
import example.component.AnswerMaker;
import example.component.QuestionMaker;
import example.component.QuizMaker;
import example.controller.CardController;
import example.controller.WorkbookController;
import example.errorcase.DuplicateParametersObject;
import example.errorcase.MultipleInjectConstructorObject;
import example.errorcase.MultipleNormalConstructorObject;
import example.repository.CardRepository;
import example.service.CardService;
import example.service.WorkbookService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConstructorInjectStrategyTest {

    private static final Class<Inject> INJECT_CLASS = Inject.class;
    private static final String ANNOTATION_NAME = INJECT_CLASS.getSimpleName();

    @DisplayName("@Inject 가 붙은 생성자가 하나 있으면 생성자 주입 전략을 지원한다.")
    @Test
    void supportsWithSingleInjectConstructor() {
        ConstructorInjectStrategy constructorInjectStrategy = new ConstructorInjectStrategy(INJECT_CLASS);
        assertThat(constructorInjectStrategy.supports(QuizMaker.class)).isTrue();
    }

    @DisplayName("@Inject 가 붙은 생성자가 여러개 존재하면 예외가 발생한다.")
    @Test
    void notSupportsWithMultipleInjectConstructor() {
        ConstructorInjectStrategy constructorInjectStrategy = new ConstructorInjectStrategy(INJECT_CLASS);
        String errorMessage = String.format("@%s가 붙어있는 생성자가 여러개 있습니다.", ANNOTATION_NAME);

        assertThatThrownBy(() -> constructorInjectStrategy.supports(MultipleInjectConstructorObject.class))
                .isInstanceOf(ImpossibleDependencyException.class)
                .hasMessageContaining(errorMessage);
    }

    @DisplayName("@Inject 가 붙은 생성자가 없는 경우, 일반 생성자가 하나만 존재한다면 생성자 주입 전략을 지원한다.")
    @Test
    void supportsWithSingleConstructor() {
        ConstructorInjectStrategy constructorInjectStrategy = new ConstructorInjectStrategy(INJECT_CLASS);
        assertThat(constructorInjectStrategy.supports(QuestionMaker.class)).isTrue();
        assertThat(constructorInjectStrategy.supports(AnswerMaker.class)).isTrue();
    }

    @DisplayName("@Inject 가 붙은 생성자가 없는 경우, 일반 생성자가 여러개 존재하면 예외가 발생한다.")
    @Test
    void notSupportsWithMultipleConstructor() {
        ConstructorInjectStrategy constructorInjectStrategy = new ConstructorInjectStrategy(INJECT_CLASS);

        assertThatThrownBy(() -> constructorInjectStrategy.supports(MultipleNormalConstructorObject.class))
                .isInstanceOf(ImpossibleDependencyException.class)
                .hasMessageContaining("생성자는 하나만 존재해야합니다");
    }

    @DisplayName("@Inject 가 붙은 생성자를 이용하여 의존성을 가진 클래스를 찾는다.")
    @Test
    void findDependenciesWithSingleInjectConstructor() {
        ConstructorInjectStrategy constructorInjectStrategy = new ConstructorInjectStrategy(Inject.class);
        Set<Class<?>> dependencies = constructorInjectStrategy.findDependencies(CardController.class);
        assertThat(dependencies).containsExactly(CardService.class);
    }

    @DisplayName("일반 생성자를 이용하여 의존성을 가진 클래스를 찾는다.")
    @Test
    void findDependenciesWithSingleConstructor() {
        ConstructorInjectStrategy constructorInjectStrategy = new ConstructorInjectStrategy(Inject.class);
        Set<Class<?>> dependencies = constructorInjectStrategy.findDependencies(WorkbookController.class);
        assertThat(dependencies).containsExactly(WorkbookService.class);
    }

    @DisplayName("생성자의 파라미터 타입이 중복되면 예외가 발생한다.")
    @Test
    void findDependenciesWithDuplicateParameters() {
        ConstructorInjectStrategy constructorInjectStrategy = new ConstructorInjectStrategy(Inject.class);

        assertThatThrownBy(() -> constructorInjectStrategy.findDependencies(DuplicateParametersObject.class))
                .isInstanceOf(ImpossibleDependencyException.class)
                .hasMessageContaining("생성자의 파라미터에 타입 중복이 있습니다");
    }

    @DisplayName("객체를 생성하여 ComponentContainer에 등록한다.")
    @Test
    void instantiate() throws ReflectiveOperationException {
        // given
        ComponentContainer componentContainer = new ComponentContainer();
        QuestionMaker questionMaker = new QuestionMaker();
        AnswerMaker answerMaker = new AnswerMaker();
        componentContainer.register(QuestionMaker.class, questionMaker);
        componentContainer.register(AnswerMaker.class, answerMaker);
        ConstructorInjectStrategy constructorInjectStrategy = new ConstructorInjectStrategy(Inject.class);

        // when
        constructorInjectStrategy.instantiate(QuizMaker.class, componentContainer);

        // then
        Object object = componentContainer.takeComponent(QuizMaker.class);
        assertThat(object).isInstanceOf(QuizMaker.class);
        assertThat(object).isNotNull();
        assertThat(((QuizMaker) object).getQuestionMaker()).isSameAs(questionMaker);
        assertThat(((QuizMaker) object).getAnswerMaker()).isSameAs(answerMaker);
    }
}
