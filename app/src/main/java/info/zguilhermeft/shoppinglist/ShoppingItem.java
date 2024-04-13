package info.zguilhermeft.shoppinglist;


public class ShoppingItem {
    private String text;
    private int id;
    private boolean checked;

    public int getId() {
        return id;
    }

    public ShoppingItem(int id, String text, boolean checked) {
        this.id = id;
        this.text = text;
        this.checked = checked;
    }

    public String getText() {
        return text;
    }

    public void setText(int id, String text) {
        this.text = text;
        this.id = id;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void toggleChecked() {
        this.checked = !this.checked;
    }
}
