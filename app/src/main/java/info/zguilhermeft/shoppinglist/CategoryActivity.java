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

    private ArrayList<Category> categories; // Lista para armazenar as categorias.
    private CategoryAdapter adapter; // Adaptador para o ListView de categorias.
    private DatabaseHelper db; // Acesso ao banco de dados.

    private ListView list; // ListView para exibir as categorias.
    private Button btn_add; // Botão para adicionar uma nova categoria.
    private Button btn_back; // Botão para voltar.
    private EditText edit_list; // Campo de texto para entrada do nome da categoria.
    private TextView text;
    ExecutorService executor = Executors.newSingleThreadExecutor(); // Executor para operações em background.

    // Carrega as categorias do banco de dados e atualiza a lista na UI.
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

            // Atualiza a UI no thread principal.
            runOnUiThread(() -> {
                categories.clear();
                categories.addAll(loadedCategories);
                adapter.notifyDataSetChanged();
            });
        });
    }

    // Adiciona uma nova categoria ao banco de dados e atualiza a lista.
    private void addItem() {
        String categoryName = edit_list.getText().toString();
        if (!categoryName.isEmpty()) {
            db.addCategory(categoryName);
            loadCategories();  // Atualiza a lista de categorias.
            edit_list.setText("");
            list.smoothScrollToPosition(categories.size() - 1);

            setResult(RESULT_OK);

            // Esconde o teclado virtual após adicionar a categoria.
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    // Configuração inicial da atividade.
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
        loadCategories(); // Carrega as categorias na inicialização.

        adapter = new CategoryAdapter(
                this,              // Contexto
                R.layout.shopping_list,  // Layout dos itens
                categories,      // Dados
                db                 // Acesso ao banco de dados
        );

        // Listener para o botão de voltar.
        btn_back.setOnClickListener(view -> {
            finish();
        });

        // Listener para o botão de adicionar categoria.
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

        list.setAdapter(adapter); // Configura o adaptador no ListView.
    }
}
