package reflection.examples;

import reflection.annotation.Inject;
import reflection.annotation.Service;

@Service
public class MyQnaService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    @Inject
    public MyQnaService(UserRepository userRepository, QuestionRepository questionRepository) {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public QuestionRepository getQuestionRepository() {
        return questionRepository;
    }
}
