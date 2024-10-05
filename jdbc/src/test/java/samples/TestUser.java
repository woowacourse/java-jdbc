package samples;

public class TestUser {

    private Long id;
    private final String account;

    public TestUser(Long id, String account) {
        this.id = id;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }
}
