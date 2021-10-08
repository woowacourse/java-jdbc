package examples;

import annotation.Component;
import annotation.Configuration;

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

