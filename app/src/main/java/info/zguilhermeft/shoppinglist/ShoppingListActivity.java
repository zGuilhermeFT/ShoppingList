package info.zguilhermeft.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.database.Cursor;

import androidx.appcompat.app.AppCompatActivity;

import com.compras.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShoppingListActivity extends AppCompatActivity {

    private ArrayList<ShoppingList> shoppingList;
    private ShoppingListAdapter adapter;
    private DatabaseHelper db;

    private ListView list;
    private Button btn_add;
    private EditText edit_list;
    private RelativeLayout main_activity;
    private boolean isFirstRenderer = true;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    private void loadShoppingLists() {
        executor.execute(() -> {
            ArrayList<ShoppingList> loadedLists = new ArrayList<>();
            Cursor cursor = db.getAllLists();
            int idIndex = cursor.getColumnIndex("id");
            int nameIndex = cursor.getColumnIndex("name");

            if (idIndex != -1 && nameIndex != -1) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    loadedLists.add(new ShoppingList(id, name)); // Use a constructor that accepts id
                }
            }
            cursor.close();

            if(!loadedLists.isEmpty() && isFirstRenderer) {
                ShoppingList lastList = loadedLists.get(loadedLists.size() - 1);

                Intent intent = new Intent(ShoppingListActivity.this, ShoppingItemActivity.class);
                intent.putExtra("listId", lastList.getId());
                startActivity(intent);
            }

            isFirstRenderer = false;

            // Update UI on main thread
            runOnUiThread(() -> {
                shoppingList.clear();
                shoppingList.addAll(loadedLists);
                adapter.notifyDataSetChanged();
                try {
                    Thread.sleep(500); // Faz a thread da UI dormir por 500 milissegundos
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Re-interrompe a thread se ela for interrompida durante o sono
                    return; // Opcional: sair do runnable se a thread for interrompida
                }
                main_activity.setVisibility(View.VISIBLE);
            });

        });
    }

    private void addItem() {
        String itemText = edit_list.getText().toString();
        if (!itemText.isEmpty()) {
            db.addList(itemText);
            loadShoppingLists();  // Refresh the list
            edit_list.setText("");
            list.smoothScrollToPosition(shoppingList.size() - 1);
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.show_categories) {
            Intent intent = new Intent(ShoppingListActivity.this, CategoryActivity.class);
            startActivity(intent);
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        db = new DatabaseHelper(this);
        list = findViewById(R.id.list);
        btn_add = findViewById(R.id.btn_add);
        edit_list = findViewById(R.id.edit_item);
        main_activity = findViewById(R.id.activity_shopping_list);

        ListView listView = findViewById(R.id.list);
        TextView emptyView = findViewById(R.id.empty_view);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        shoppingList = new ArrayList<>();
        loadShoppingLists(); // Load lists from database

        adapter = new ShoppingListAdapter(
                this,              // Context
                R.layout.shopping_list,  // ID do layout do item
                shoppingList,      // Lista de dados
                db                 // Instância de DatabaseHelper
        );

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

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Intent intent = new Intent(ShoppingListActivity.this, ShoppingItemActivity.class);
                intent.putExtra("listId", shoppingList.get(pos).getId());
                startActivity(intent);
            }
        });
    }
}
