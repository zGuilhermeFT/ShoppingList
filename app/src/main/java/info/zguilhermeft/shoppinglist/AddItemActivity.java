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

    private EditText editTextItemName;
    private EditText editTextQuantity;
    private Spinner spinnerCategory;
    private CheckBox checkboxPurchased;
    private Button buttonAddItem;
    private Button buttonCancel;
    private Button buttonAddCategory;
    private DatabaseHelper db;
    private int listId;
    public ActivityResultLauncher<Intent> categoryActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        listId = extras.getInt("listId");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        db = new DatabaseHelper(this);

        editTextItemName = findViewById(R.id.editTextItemName);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        checkboxPurchased = findViewById(R.id.checkboxPurchased);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        buttonCancel = findViewById(R.id.cancel_button);

        loadCategoriesIntoSpinner();

        buttonCancel.setOnClickListener(v -> finish());
        buttonAddItem.setOnClickListener(v -> addItem());
        buttonAddCategory = findViewById(R.id.buttonAddCategory);

        categoryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadCategoriesIntoSpinner();
                        setResult(RESULT_OK);
                    }
                }
        );

        buttonAddCategory.setOnClickListener(view -> {
            Intent intent = new Intent(AddItemActivity.this, CategoryActivity.class);
            categoryActivityResultLauncher.launch(intent);
        });
    }
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
