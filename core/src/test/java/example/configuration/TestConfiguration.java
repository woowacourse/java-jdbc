package example.configuration;

import di.annotation.Component;
import di.annotation.Configuration;
import example.repository.CardRepository;

@Configuration
public class TestConfiguration {

    @Component
    public DummyDataSource dummyDataSource(CardRepository cardRepository) {
        return new DummyDataSource("url", "userName", "password");
    }

    public String fakeMethod() {
        return "fake";
    }
}

