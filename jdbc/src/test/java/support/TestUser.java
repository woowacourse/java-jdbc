package support;

public class TestUser {

    private Long id;
    private final String name;

    public TestUser(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public TestUser(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
