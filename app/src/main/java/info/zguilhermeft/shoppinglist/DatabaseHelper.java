package info.zguilhermeft.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ShoppingListDB";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_LISTS = "lists";
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_ITEMS = "items";

    private static final String CREATE_TABLE_LISTS =
            "CREATE TABLE " + TABLE_LISTS + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT);";

    private static final String CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT);";

    private static final String CREATE_TABLE_ITEMS =
            "CREATE TABLE " + TABLE_ITEMS + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "list_id INTEGER," +
                    "name TEXT," +
                    "quantity INTEGER," +
                    "category_id INTEGER," +
                    "purchased INTEGER," +  // 0 for not purchased, 1 for purchased
                    "FOREIGN KEY(list_id) REFERENCES lists(id)," +
                    "FOREIGN KEY(category_id) REFERENCES categories(id));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LISTS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_ITEMS);

        // Insert initial categories after creating the categories table
        insertInitialCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTS);
        onCreate(db);
    }

    private void insertInitialCategories(SQLiteDatabase db) {
        String[] categories = {"Outros", "Comida", "Eletrônicos", "Roupas", "Eletrodomésticos"};
        for (String category : categories) {
            ContentValues values = new ContentValues();
            values.put("name", category);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    public void addList(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        db.insert(TABLE_LISTS, null, values);
        db.close();
    }

    public void updateList(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        db.update(TABLE_LISTS, values, "id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    public void deleteList(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LISTS, "id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    public Cursor getAllLists() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_LISTS, null);
    }

    public void addItem(int listId, String name, int quantity, int categoryId, boolean purchased) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("list_id", listId);
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("category_id", categoryId);
        values.put("purchased", purchased ? 1 : 0);
        db.insert(TABLE_ITEMS, null, values);
        db.close();
    }

    public Cursor getItem(int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(itemId)});
    }
    public void updateItem(int itemId, String name, int quantity, int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("category_id", categoryId);
        db.update(TABLE_ITEMS, values, "id = ?", new String[] { String.valueOf(itemId) });
        db.close();
    }

    public void togglePurchasedItem(int itemId, boolean purchased) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("purchased", purchased ? 1 : 0);
        db.update(TABLE_ITEMS, values, "id = ?", new String[] { String.valueOf(itemId) });
        db.close();
    }

    public void deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, "id = ?", new String[] { String.valueOf(itemId) });
        db.close();
    }

    public Cursor getAllItems(int listId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ITEMS + " WHERE list_id = " + listId, null);
    }

    public Cursor getAllItemsAndCategories(int listId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT *, " + TABLE_ITEMS + ".id AS id, " + TABLE_ITEMS + ".name AS itemName, " + TABLE_CATEGORIES + ".name AS categoryName FROM " + TABLE_ITEMS + " LEFT JOIN " + TABLE_CATEGORIES  + " ON " + TABLE_ITEMS + ".category_id = " + TABLE_CATEGORIES + ".id WHERE list_id = " + listId, null);
    }

    public void addCategory(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        db.insert(TABLE_CATEGORIES, null, values);
        db.close();
    }

    public void updateCategory(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        db.update(TABLE_CATEGORIES, values, "id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    public void deleteCategory(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORIES, "id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    public Cursor getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES, null);
    }
}
