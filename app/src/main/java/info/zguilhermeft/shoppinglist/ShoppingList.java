package info.zguilhermeft.shoppinglist;

public class ShoppingList {
    private int id;
    private String text;

    public ShoppingList(int id, String text) {
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

}
