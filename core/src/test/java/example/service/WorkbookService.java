package example.service;


import di.annotation.Inject;
import di.annotation.Service;
import example.component.QuizMaker;
import example.repository.TagRepository;
import example.repository.WorkbookRepository;

@Service
public class WorkbookService {

    private final TagRepository tagRepository;
    private final WorkbookRepository workbookRepository;
    private final CardService cardService;
    private final QuizMaker quizMaker;

    @Inject
    public WorkbookService(TagRepository tagRepository, WorkbookRepository workbookRepository, CardService cardService, QuizMaker quizMaker) {
        this.tagRepository = tagRepository;
        this.workbookRepository = workbookRepository;
        this.cardService = cardService;
        this.quizMaker = quizMaker;
    }

    public WorkbookService(String what) {
        // fake constructor
        throw new IllegalStateException();
    }
}
