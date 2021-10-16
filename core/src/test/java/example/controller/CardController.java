package example.controller;


import di.annotation.Controller;
import di.annotation.Inject;
import example.service.CardService;

@Controller
public class CardController {

    private final CardService cardService;

    @Inject
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    public CardController(Integer integer) {
        // fake constructor
        throw new IllegalStateException();
    }
}
