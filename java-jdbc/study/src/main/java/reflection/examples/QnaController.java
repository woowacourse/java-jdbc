package reflection.examples;

import reflection.annotation.Controller;
import reflection.annotation.Inject;

@Controller
public class QnaController {

    private final MyQnaService qnaService;

    @Inject
    public QnaController(MyQnaService qnaService) {
        this.qnaService = qnaService;
    }
}
