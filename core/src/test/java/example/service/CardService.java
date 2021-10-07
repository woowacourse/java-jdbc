package example.service;


import di.annotation.Inject;
import di.annotation.Service;
import example.repository.CardRepository;

@Service
public class CardService {

    @Inject
    private final CardRepository cardRepository;

    private final String noInjectObject;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
        noInjectObject = "";
    }
}
