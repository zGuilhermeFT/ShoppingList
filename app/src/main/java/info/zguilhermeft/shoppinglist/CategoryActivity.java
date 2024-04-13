package info.zguilhermeft.shoppinglist;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.compras.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryActivity extends AppCompatActivity {

    private ArrayList<Category> categories;
    private CategoryAdapter adapter;
    private DatabaseHelper db;

    private ListView list;
    private Button btn_add;
    private Button btn_back;
    private EditText edit_list;
    private TextView text;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    private void loadCategories() {
        executor.execute(() -> {
            ArrayList<Category> loadedCategories = new ArrayList<>();
            Cursor cursor = db.getAllCategories();
            int idIndex = cursor.getColumnIndex("id");
            int nameIndex = cursor.getColumnIndex("name");

            if (idIndex != -1 && nameIndex != -1) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    loadedCategories.add(new Category(id, name));
                }
            }
            cursor.close();

            // Update UI on main thread
            runOnUiThread(() -> {
                categories.clear();
                categories.addAll(loadedCategories);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void addItem() {
        String categoryName = edit_list.getText().toString();
        if (!categoryName.isEmpty()) {
            db.addCategory(categoryName);
            loadCategories();  // Refresh the category list
            edit_list.setText("");
            list.smoothScrollToPosition(categories.size() - 1);

            setResult(RESULT_OK);

            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        db = new DatabaseHelper(this);
        list = findViewById(R.id.list);
        btn_add = findViewById(R.id.btn_add);
        edit_list = findViewById(R.id.edit_item);
        btn_back = findViewById(R.id.btn_back);

        ListView listView = findViewById(R.id.list);
        TextView emptyView = findViewById(R.id.empty_view);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        categories = new ArrayList<>();
        loadCategories(); // Load lists from database

        adapter = new CategoryAdapter(
                this,              // Context
                R.layout.shopping_list,  // ID do layout do item
                categories,      // Lista de dados
                db                 // Instância de DatabaseHelper
        );


        btn_back.setOnClickListener(view -> {
            finish();
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });
        edit_list.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                addItem();
                return true;
            }
        });

        list.setAdapter(adapter);
    }
}
