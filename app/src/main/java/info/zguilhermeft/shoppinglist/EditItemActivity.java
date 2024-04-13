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

import com.compras.R;

import java.util.ArrayList;
import java.util.List;

public class EditItemActivity extends AppCompatActivity {

    private EditText editTextItemName;
    private EditText editTextQuantity;
    private Spinner spinnerCategory;
    private CheckBox checkboxPurchased;
    private Button buttonUpdateItem;
    private Button buttonCancel;
    private Button buttonAddCategory;
    private DatabaseHelper db;
    private int itemId;
    public ActivityResultLauncher<Intent> categoryActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);  // Consider renaming this layout to something generic

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        itemId = extras.getInt("itemId");

        db = new DatabaseHelper(this);

        editTextItemName = findViewById(R.id.editTextItemName);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        checkboxPurchased = findViewById(R.id.checkboxPurchased);
        buttonUpdateItem = findViewById(R.id.buttonAddItem);
        buttonCancel = findViewById(R.id.cancel_button);
        buttonAddCategory = findViewById(R.id.buttonAddCategory);
        buttonUpdateItem.setText(R.string.edit_item); // Change button text to "Atualizar item"

        loadCategoriesIntoSpinner();
        loadItemData();

        buttonCancel.setOnClickListener(v -> finish());
        buttonUpdateItem.setOnClickListener(v -> updateItem());

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
            Intent intent = new Intent(EditItemActivity.this, CategoryActivity.class);
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
    private void loadItemData() {
        Cursor cursor = db.getItem(itemId);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex("name");
            int quantityIndex = cursor.getColumnIndex("quantity");
            int categoryIdIndex = cursor.getColumnIndex("category_id");
            int purchasedIndex = cursor.getColumnIndex("purchased");

            if (nameIndex != -1) {
                editTextItemName.setText(cursor.getString(nameIndex));
            }
            if (quantityIndex != -1) {
                editTextQuantity.setText(cursor.getString(quantityIndex));
            }
            if (categoryIdIndex != -1) {
                int categoryId = cursor.getInt(categoryIdIndex);
                setSpinnerToCategory(categoryId);  // Ajuste para encontrar a posição correta
            }
            if (purchasedIndex != -1) {
                checkboxPurchased.setChecked(cursor.getInt(purchasedIndex) == 1);
            }
            cursor.close();
        } else {
            Toast.makeText(this, "Nada foi encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void setSpinnerToCategory(int categoryId) {
        ArrayAdapter<Category> adapter = (ArrayAdapter<Category>) spinnerCategory.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if (adapter.getItem(position).getId() == categoryId) {
                spinnerCategory.setSelection(position);
                break;
            }
        }
    }



    private void updateItem() {
        if (spinnerCategory.getCount() == 0) {
            Toast.makeText(this, "Não há categorias disponíveis.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String name = editTextItemName.getText().toString().trim();
            int quantity = Integer.parseInt(editTextQuantity.getText().toString().trim());
            Category selectedCategory = (Category) spinnerCategory.getSelectedItem();

            if (selectedCategory == null) {
                Toast.makeText(this, "Nenhuma categoria selecionada.", Toast.LENGTH_SHORT).show();
                return;
            }
            int categoryId = selectedCategory.getId();
            boolean purchased = checkboxPurchased.isChecked();

            if (name.isEmpty()) {
                Toast.makeText(this, "Por favor insira o nome do item.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.updateItem(itemId, name, quantity, categoryId, purchased);
            Toast.makeText(this, "Item editado com sucesso!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor verifique os valores insridos.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao atualizar o item: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
