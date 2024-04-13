package info.zguilhermeft.shoppinglist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.compras.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShoppingItemActivity extends AppCompatActivity {

    private static final String FILENAME = "shopping_list.txt";
    private static final int MAX_BYTES = 8000;

    private ArrayList<ShoppingItem> itemList;
    private ShoppingItemAdapter adapter;

    private ListView list;
    private Button btn_add;
    private Button btn_back;
    private int listId;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private DatabaseHelper db;
    public ActivityResultLauncher<Intent> addItemActivityResultLauncher;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {  // Verifica se o resultado vem da AddItemActivity com o código 1
            if (resultCode == RESULT_OK) {
                loadItemLists();  // Recarrega a lista de itens
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        listId = extras.getInt("listId");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_item);

        db = new DatabaseHelper(this);
        list = findViewById(R.id.list);
        btn_add = findViewById(R.id.btn_add);
        btn_back = findViewById(R.id.btn_back);

        ListView listView = findViewById(R.id.list);
        TextView emptyView = findViewById(R.id.empty_view);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        itemList = new ArrayList<>();
        loadItemLists(); // Load lists from database

        adapter = new ShoppingItemAdapter(
                this,              // Context
                R.layout.shopping_list,  // ID do layout do item
                itemList,      // Lista de dados
                db                 // Instância de DatabaseHelper
        );

        addItemActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadItemLists();  // Recarrega a lista de itens se um novo foi adicionado
                    }
                }
        );

        btn_add.setOnClickListener(view -> {
            Intent intent = new Intent(ShoppingItemActivity.this, AddItemActivity.class);
            intent.putExtra("listId", listId);
            addItemActivityResultLauncher.launch(intent);
        });

        btn_back.setOnClickListener(view -> {
            finish();
        });

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Log.i("zguilhermeft", "onItemClick");
                itemList.get(pos).toggleChecked();
                db.togglePurchasedItem(itemList.get(pos).getId(), itemList.get(pos).isChecked());
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void loadItemLists() {
        executor.execute(() -> {
            ArrayList<ShoppingItem> loadedLists = new ArrayList<>();
            Cursor cursor = db.getAllItemsAndCategories(listId);
            int idIndex = cursor.getColumnIndex("id");
            int nameIndex = cursor.getColumnIndex("itemName");
            int categoryIndex = cursor.getColumnIndex("categoryName");
            int quantityIndex = cursor.getColumnIndex("quantity");
            int purchasedIndex = cursor.getColumnIndex("purchased");
            if (idIndex != -1 && nameIndex != -1) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idIndex);
                    String name = "Nome: " + cursor.getString(nameIndex) + (cursor.getString(categoryIndex) != null ? "\n\nCategoria: " + cursor.getString(categoryIndex) : "") + "\n\nQuantidade: " + cursor.getInt(quantityIndex);
                    int checked = cursor.getInt(purchasedIndex);
                    loadedLists.add(new ShoppingItem(id, name, checked == 1)); // Use a constructor that accepts id
                }
            }
            cursor.close();

            // Update UI on main thread
            runOnUiThread(() -> {
                itemList.clear();
                itemList.addAll(loadedLists);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int itemID = item.getItemId();

        if(itemID == R.id.clear_checked) {
            clearChecked();
            return true;
        }
        else if(itemID == R.id.clear_all) {
            clearAll();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void clearAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm);
        builder.setMessage(R.string.confirm_clear_all);
        builder.setPositiveButton(R.string.clear_all, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                itemList.clear();
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void clearChecked() {
        int i = 0;
        while (i < itemList.size()) {
            if (itemList.get(i).isChecked()) {
                itemList.remove(i);
            } else {
                i++;
            }
        }
        adapter.notifyDataSetChanged();
    }
}
