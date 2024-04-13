package info.zguilhermeft.shoppinglist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.compras.R;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<Category> {
    private DatabaseHelper db;

    // Construtor do adaptador
    public CategoryAdapter(Context context, int resource, List<Category> objects, DatabaseHelper db) {
        super(context, resource, objects);
        this.db = db;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result = convertView;
        if (result == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            result = inflater.inflate(R.layout.shopping_list, parent, false);
        }

        // Vinculação dos elementos de UI
        TextView text = result.findViewById(R.id.shopping_list);
        Button btnEdit = result.findViewById(R.id.btn_edit);
        Button btnRemove = result.findViewById(R.id.btn_remove);

        // Configuração dos dados da categoria no texto do item
        Category item = getItem(position);
        text.setText(item.getText());

        // Listener para edição de categoria
        btnEdit.setOnClickListener(v -> {
            editItem(position);
        });

        // Listener para remoção de categoria
        btnRemove.setOnClickListener(v -> {
            maybeRemoveItem(position);
        });

        return result;
    }

    // Método para tentativa de remoção de uma categoria
    private void maybeRemoveItem(int position) {
        Category item = getItem(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.confirm);
        String fmt = getContext().getResources().getString(R.string.confirm_message, item.getText());
        builder.setMessage(fmt);
        builder.setPositiveButton(R.string.remove, (dialog, which) -> {
            int listId = item.getId();
            db.deleteCategory(listId);
            remove(item);
            notifyDataSetChanged();

            // Retorna o resultado para a atividade chamadora
            if (getContext() instanceof CategoryActivity) {
                ((CategoryActivity) getContext()).setResult(Activity.RESULT_OK);
            } else {
                throw new RuntimeException("The context used isn't CategoryActivity");
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    // Método para editar uma categoria
    private void editItem(int position) {
        Category item = getItem(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.edit_category);

        // Configura o campo de entrada de texto
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(item.getText());
        builder.setView(input);

        // Configura os botões do diálogo
        builder.setPositiveButton("Save", (dialog, which) -> {
            String itemName = input.getText().toString();
            if (!itemName.isEmpty()) {
                item.setText(itemName);
                db.updateCategory(item.getId(), itemName);
                notifyDataSetChanged();

                // Retorna o resultado para a atividade chamadora
                if (getContext() instanceof CategoryActivity) {
                    ((CategoryActivity) getContext()).setResult(Activity.RESULT_OK);
                } else {
                    throw new RuntimeException("The context used isn't CategoryActivity");
                }

                Toast.makeText(getContext(), "Categoria atualizada com sucesso", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Nome da categoria é obrigatório", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
