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

    // Lista para armazenar objetos ShoppingList.
    private ArrayList<ShoppingList> shoppingList;
    // Adaptador para gerenciar a apresentação dos objetos ShoppingList em uma ListView.
    private ShoppingListAdapter adapter;
    // Helper para interação com o banco de dados.
    private DatabaseHelper db;

    // Referências para os componentes da interface do usuário.
    private ListView list;
    private Button btn_add;
    private EditText edit_list;
    private RelativeLayout main_activity;
    // Flag para verificar se é a primeira renderização da atividade.
    private boolean isFirstRenderer = true;
    // Executor para realizar operações assíncronas.
    ExecutorService executor = Executors.newSingleThreadExecutor();

    // Método para carregar as listas de compras do banco de dados de forma assíncrona.
    private void loadShoppingLists() {
        executor.execute(() -> {
            ArrayList<ShoppingList> loadedLists = new ArrayList<>();
            Cursor cursor = db.getAllLists();
            int idIndex = cursor.getColumnIndex("id");
            int nameIndex = cursor.getColumnIndex("name");

            // Lê os dados do cursor e cria objetos ShoppingList.
            if (idIndex != -1 && nameIndex != -1) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    loadedLists.add(new ShoppingList(id, name));
                }
            }
            cursor.close();

            // Se a lista não estiver vazia e for a primeira renderização, abre a última lista adicionada.
            if(!loadedLists.isEmpty() && isFirstRenderer) {
                ShoppingList lastList = loadedLists.get(loadedLists.size() - 1);

                Intent intent = new Intent(ShoppingListActivity.this, ShoppingItemActivity.class);
                intent.putExtra("listId", lastList.getId());
                startActivity(intent);
            }

            isFirstRenderer = false;

            // Atualiza a UI na thread principal após carregar os dados.
            runOnUiThread(() -> {
                shoppingList.clear();
                shoppingList.addAll(loadedLists);
                adapter.notifyDataSetChanged();
                try {
                    Thread.sleep(500); // Pausa a UI por 500ms para processamento.
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Interrompe a thread se for interrompida.
                    return;
                }
                main_activity.setVisibility(View.VISIBLE); // Torna a atividade visível após o carregamento.
            });

        });
    }

    // Método para adicionar uma nova lista de compras.
    private void addItem() {
        String itemText = edit_list.getText().toString();
        if (!itemText.isEmpty()) {
            db.addList(itemText);
            loadShoppingLists();  // Recarrega as listas para refletir a adição.
            edit_list.setText("");
            list.smoothScrollToPosition(shoppingList.size() - 1); // Desloca a lista para a nova adição.
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0); // Oculta o teclado virtual.
            }
        }
    }

    // Método para criar o menu de opções.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    // Método para lidar com eventos de cliques nos itens do menu.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.show_categories) {
            Intent intent = new Intent(ShoppingListActivity.this, CategoryActivity.class);
            startActivity(intent);
        }

        return true;
    }

    // Método chamado ao criar a atividade, inicializa componentes e carrega dados.
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
        loadShoppingLists(); // Carrega as listas do banco de dados.

        adapter = new ShoppingListAdapter(
                this,              // Contexto
                R.layout.shopping_list,  // ID do layout do item
                shoppingList,      // Lista de dados
                db                 // Instância de DatabaseHelper
        );

        // Configura o botão para adicionar novas listas de compras.
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });

        // Configura o editor de texto para adicionar listas ao pressionar 'Enter'.
        edit_list.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                addItem();
                return true;
            }
        });

        // Configura o evento de clique para abrir itens da lista.
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

