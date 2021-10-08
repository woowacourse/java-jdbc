package example.configuration;

import di.annotation.Component;
import di.annotation.Configuration;

@Configuration
public class TestConfiguration {

    @Component
    public DummyDataSource dummyDataSource() {
        return new DummyDataSource("url", "userName", "password");
    }

    public String fakeMethod() {
        return "fake";
    }
}

