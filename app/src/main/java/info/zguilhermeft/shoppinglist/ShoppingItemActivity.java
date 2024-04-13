package info.zguilhermeft.shoppinglist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
    // Lista para armazenar os itens de compra.
    private ArrayList<ShoppingItem> itemList;
    // Adaptador para gerenciar a apresentação dos itens na ListView.
    private ShoppingItemAdapter adapter;

    // Referência para o componente ListView no layout.
    private ListView list;
    // Botões para adicionar novos itens e voltar.
    private Button btn_add;
    private Button btn_back;
    // Identificador da lista de compras, passado como extra na Intent.
    private int listId;
    // Executor para rodar processos em um thread separado.
    ExecutorService executor = Executors.newSingleThreadExecutor();
    // Helper para interação com o banco de dados.
    private DatabaseHelper db;
    // Lançador de resultados para a atividade de adicionar itens.
    public ActivityResultLauncher<Intent> addItemActivityResultLauncher;

    // Método chamado quando uma atividade lançada para resultado retorna.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Verifica se o resultado veio da atividade de adicionar item.
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // Recarrega a lista de itens após adicionar um novo.
                loadItemLists();
            }
        }
    }

    // Método chamado ao criar a atividade.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        listId = extras.getInt("listId");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_item);

        db = new DatabaseHelper(this);

        // Consulta para obter o nome da lista e definir como título da atividade.
        Cursor cursor = db.getList(listId);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex("name");
            setTitle("Lista: " + cursor.getString(nameIndex));
            cursor.close();
        } else {
            Toast.makeText(this, "Nada foi encontrado", Toast.LENGTH_SHORT).show();
        }

        list = findViewById(R.id.list);
        btn_add = findViewById(R.id.btn_add);
        btn_back = findViewById(R.id.btn_back);

        ListView listView = findViewById(R.id.list);
        TextView emptyView = findViewById(R.id.empty_view);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        itemList = new ArrayList<>();
        loadItemLists(); // Carrega a lista de itens do banco de dados.

        adapter = new ShoppingItemAdapter(
                this,                 // Contexto
                R.layout.shopping_list,  // Layout do item
                itemList,              // Lista de dados
                db                     // Instância do DatabaseHelper
        );

        // Configura o lançador de atividades para adicionar itens.
        addItemActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Recarrega os itens se um novo foi adicionado.
                        loadItemLists();
                    }
                }
        );

        // Define os listeners para os botões de adicionar e voltar.
        btn_add.setOnClickListener(view -> {
            Intent intent = new Intent(ShoppingItemActivity.this, AddItemActivity.class);
            intent.putExtra("listId", listId);
            addItemActivityResultLauncher.launch(intent);
        });

        btn_back.setOnClickListener(view -> {
            finish();
        });

        list.setAdapter(adapter);

        // Listener para ação de clique nos itens da lista.
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Log.i("zguilhermeft", "onItemClick");
                // Alterna o estado de comprado do item clicado.
                itemList.get(pos).toggleChecked();
                // Atualiza o estado no banco de dados.
                db.togglePurchasedItem(itemList.get(pos).getId(), itemList.get(pos).isChecked());
                // Notifica o adaptador que os dados mudaram para atualizar a UI.
                adapter.notifyDataSetChanged();
            }
        });
    }

    // Carrega e atualiza a lista de itens de compras de forma assíncrona.
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
                    loadedLists.add(new ShoppingItem(id, name, checked == 1));
                }
            }
            cursor.close();

            // Atualiza a UI na thread principal após carregar os dados.
            runOnUiThread(() -> {
                itemList.clear();
                itemList.addAll(loadedLists);
                adapter.notifyDataSetChanged();
            });
        });
    }

    // Cria o menu de opções.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_with_clear, menu);
        return true;
    }

    // Manipula os eventos de clique nos itens do menu.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.show_categories) {
            // Lança a atividade de categorias.
            Intent intent = new Intent(ShoppingItemActivity.this, CategoryActivity.class);
            addItemActivityResultLauncher.launch(intent);
        }
        if(item.getItemId() == R.id.clear_checked) {
            // Diálogo de confirmação para limpar itens marcados como comprados.
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(R.string.confirm);
            String fmt = getResources().getString(R.string.confirm_message_buy);
            builder.setMessage(fmt);
            builder.setPositiveButton(R.string.remove, (dialog, which) -> {
                // Limpa a lista e itens comprados no banco de dados.
                db.deletePurchasedItems(listId);
                loadItemLists();
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
        }

        return true;
    }
}

