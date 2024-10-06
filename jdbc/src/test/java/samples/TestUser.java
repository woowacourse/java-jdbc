package samples;

public class TestUser {

    private final Long id;
    private final String name;

    public TestUser(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
