package sample;

public class Sample {

    private Long id;
    private String name;
    private String note;

    public Sample(Long id, String name, String note) {
        this.id = id;
        this.name = name;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNote() {
        return note;
    }
}
