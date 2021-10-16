package example.component;


import di.annotation.Component;
import di.annotation.Inject;

@Component
public class QuizMaker {

    private final AnswerMaker answerMaker;
    private final QuestionMaker questionMaker;

    @Inject
    public QuizMaker(AnswerMaker answerMaker, QuestionMaker questionMaker) {
        this.answerMaker = answerMaker;
        this.questionMaker = questionMaker;
    }

    public AnswerMaker getAnswerMaker() {
        return answerMaker;
    }

    public QuestionMaker getQuestionMaker() {
        return questionMaker;
    }
}
