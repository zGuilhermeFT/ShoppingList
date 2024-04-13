package info.zguilhermeft.shoppinglist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import com.compras.R;

public class AddItemActivity extends AppCompatActivity {

    // Declaração de variáveis para manipulação dos elementos da interface.
    private EditText editTextItemName;
    private EditText editTextQuantity;
    private Spinner spinnerCategory;
    private CheckBox checkboxPurchased;
    private Button buttonAddItem;
    private Button buttonCancel;
    private Button buttonAddCategory;
    private DatabaseHelper db;
    private int listId; // ID da lista a que o item pertencerá.
    public ActivityResultLauncher<Intent> categoryActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        assert extras != null; // Garante que os extras não sejam nulos.
        listId = extras.getInt("listId"); // Obtém o ID da lista passado como extra.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item); // Define o layout da atividade.

        db = new DatabaseHelper(this); // Instancia o helper do banco de dados.

        // Inicialização dos componentes da interface com base no layout.
        editTextItemName = findViewById(R.id.editTextItemName);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        checkboxPurchased = findViewById(R.id.checkboxPurchased);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        buttonCancel = findViewById(R.id.cancel_button);

        loadCategoriesIntoSpinner(); // Carrega as categorias disponíveis no spinner.

        buttonCancel.setOnClickListener(v -> finish()); // Define o listener para fechar a atividade.
        buttonAddItem.setOnClickListener(v -> addItem()); // Define o listener para adicionar um item.
        buttonAddCategory = findViewById(R.id.buttonAddCategory);

        // Configura o launcher para resultados de atividade, utilizado para adicionar categorias.
        categoryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadCategoriesIntoSpinner(); // Recarrega as categorias se uma nova foi adicionada.
                        setResult(RESULT_OK);
                    }
                }
        );

        // Define o listener para abrir a atividade de adição de categoria.
        buttonAddCategory.setOnClickListener(view -> {
            Intent intent = new Intent(AddItemActivity.this, CategoryActivity.class);
            categoryActivityResultLauncher.launch(intent);
        });
    }

    // Carrega as categorias do banco de dados e as adiciona ao spinner.
    private void loadCategoriesIntoSpinner() {
        Cursor cursor = db.getAllCategories();
        List<Category> categories = new ArrayList<>();
        int idIndex = cursor.getColumnIndex("id");
        int nameIndex = cursor.getColumnIndex("name");

        if (idIndex != -1 && nameIndex != -1) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(idIndex);
                String text = cursor.getString(nameIndex);
                categories.add(new Category(id, text));
            }
        }
        cursor.close();

        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    // Adiciona um novo item ao banco de dados usando os valores inseridos nos campos de entrada.
    private void addItem() {
        try {
            String name = editTextItemName.getText().toString().trim();
            int quantity = Integer.parseInt(editTextQuantity.getText().toString().trim());

            // Obter o objeto Category selecionado no Spinner
            Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
            if (selectedCategory == null) {
                Toast.makeText(this, "Selecione uma categoria.", Toast.LENGTH_SHORT).show();
                return;
            }
            int categoryId = selectedCategory.getId(); // Usar o ID real da categoria
            boolean purchased = checkboxPurchased.isChecked();

            if (name.isEmpty()) {
                Toast.makeText(this, "Por favor insira o nome do item.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.addItem(listId, name, quantity, categoryId, purchased);
            Toast.makeText(this, "Item adicionado com sucesso!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor verifique os valores insridos.", Toast.LENGTH_SHORT).show();
        }
    }

}
