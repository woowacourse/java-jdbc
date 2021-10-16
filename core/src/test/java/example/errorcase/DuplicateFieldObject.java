package example.errorcase;

import di.annotation.Inject;
import example.repository.CardRepository;

public class DuplicateFieldObject {

    @Inject
    private CardRepository cardRepository;

    @Inject
    private CardRepository cardRepository2;

}
