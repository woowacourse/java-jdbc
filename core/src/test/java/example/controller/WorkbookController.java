package example.controller;


import di.annotation.Controller;
import example.service.WorkbookService;

@Controller
public class WorkbookController {

    private final WorkbookService workbookService;

    public WorkbookController(WorkbookService workbookService) {
        this.workbookService = workbookService;
    }
}
