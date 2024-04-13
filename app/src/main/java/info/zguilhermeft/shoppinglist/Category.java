package info.zguilhermeft.shoppinglist;

public class Category {
    private int id;
    private String text;

    public Category(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;  // This should return the name, not the object hash
    }
}
