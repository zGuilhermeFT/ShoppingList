package info.zguilhermeft.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Nome do banco de dados e sua versão.
    private static final String DATABASE_NAME = "ShoppingListDB";
    private static final int DATABASE_VERSION = 1;

    // Nomes das tabelas do banco de dados.
    private static final String TABLE_LISTS = "lists";
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_ITEMS = "items";

    // SQL para criar tabelas no banco de dados.
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
                    "purchased INTEGER," +  // 0 para não comprado, 1 para comprado
                    "FOREIGN KEY(list_id) REFERENCES lists(id)," +
                    "FOREIGN KEY(category_id) REFERENCES categories(id));";

    // Construtor para inicializar o SQLiteOpenHelper.
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Cria as tabelas ao criar o banco de dados.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LISTS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_ITEMS);

        // Insere categorias iniciais após criar a tabela de categorias.
        insertInitialCategories(db);
    }

    // Atualiza o banco de dados ao mudar de versão.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTS);
        onCreate(db);
    }

    // Insere categorias iniciais no banco de dados.
    private void insertInitialCategories(SQLiteDatabase db) {
        String[] categories = {"Outros", "Comida", "Eletrônicos", "Roupas", "Eletrodomésticos"};
        for (String category : categories) {
            ContentValues values = new ContentValues();
            values.put("name", category);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    // Adiciona uma nova lista ao banco de dados.
    public void addList(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        db.insert(TABLE_LISTS, null, values);
        db.close();
    }

    // Atualiza o nome de uma lista existente.
    public void updateList(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        db.update(TABLE_LISTS, values, "id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    // Exclui uma lista do banco de dados.
    public void deleteList(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LISTS, "id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    // Retorna todos as listas do banco de dados.
    public Cursor getAllLists() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_LISTS, null);
    }

    // Retorna uma lista específica do banco de dados.
    public Cursor getList(int listId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_LISTS + " WHERE id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(listId)});
    }

    // Adiciona um novo item ao banco de dados.
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

    // Retorna um item específico do banco de dados.
    public Cursor getItem(int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(itemId)});
    }

    // Atualiza um item específico no banco de dados.
    public void updateItem(int itemId, String name, int quantity, int categoryId, boolean purchased) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("category_id", categoryId);
        values.put("purchased", purchased ? 1 : 0);
        db.update(TABLE_ITEMS, values, "id = ?", new String[] { String.valueOf(itemId) });
        db.close();
    }

    // Alterna o status de comprado de um item no banco de dados.
    public void togglePurchasedItem(int itemId, boolean purchased) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("purchased", purchased ? 1 : 0);
        db.update(TABLE_ITEMS, values, "id = ?", new String[] { String.valueOf(itemId) });
        db.close();
    }

    // Exclui um item específico.
    public void deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, "id = ?", new String[] { String.valueOf(itemId) });
        db.close();
    }

    // Exclui todos os itens comprados de uma lista específica.
    public void deletePurchasedItems(int listId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, "list_id = ? AND purchased = 1", new String[] { String.valueOf(listId) });
        db.close();
    }

    // Retorna todos os itens e suas categorias associadas de uma lista específica.
    public Cursor getAllItemsAndCategories(int listId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT *, " + TABLE_ITEMS + ".id AS id, " + TABLE_ITEMS + ".name AS itemName, " + TABLE_CATEGORIES + ".name AS categoryName FROM " + TABLE_ITEMS + " LEFT JOIN " + TABLE_CATEGORIES  + " ON " + TABLE_ITEMS + ".category_id = " + TABLE_CATEGORIES + ".id WHERE list_id = " + listId, null);
    }

    // Adiciona uma nova categoria ao banco de dados.
    public void addCategory(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        db.insert(TABLE_CATEGORIES, null, values);
        db.close();
    }

    // Atualiza o nome de uma categoria existente.
    public void updateCategory(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        db.update(TABLE_CATEGORIES, values, "id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    // Exclui uma categoria do banco de dados.
    public void deleteCategory(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORIES, "id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    // Retorna todas as categorias disponíveis no banco de dados.
    public Cursor getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES, null);
    }
}
