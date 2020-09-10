package sample.entity;

public class ExcelDataVO {


    private String name_a;
    private String name_b;

    public ExcelDataVO(String name_a, String name_b) {
        this.name_a = name_a;
        this.name_b = name_b;
    }

    public String getName_a() {
        return name_a;
    }

    public void setName_a(String name_a) {
        this.name_a = name_a;
    }

    public String getName_b() {
        return name_b;
    }

    public void setName_b(String name_b) {
        this.name_b = name_b;
    }
}
