package example.service;


import di.annotation.Inject;
import di.annotation.Service;
import example.repository.CardRepository;

@Service
public class CardService {

    @Inject
    private CardRepository cardRepository;

    private String noInjectObject;

    public CardRepository getCardRepository() {
        return cardRepository;
    }
}
