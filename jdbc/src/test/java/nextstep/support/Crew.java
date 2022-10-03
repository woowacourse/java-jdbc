package nextstep.support;

public class Crew {

    private Long id;
    private String nickname;
    private String name;
    private int age;

    public Crew(final Long id, final String nickname, final String name, final int age) {
        this.id = id;
        this.nickname = nickname;
        this.name = name;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
