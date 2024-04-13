package info.zguilhermeft.shoppinglist;

import android.app.AlertDialog;
import android.content.Context;
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

public class ShoppingListAdapter extends ArrayAdapter<ShoppingList> {
    private DatabaseHelper db;

    public ShoppingListAdapter(Context context, int resource, List<ShoppingList> objects, DatabaseHelper db) {
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

        TextView text = result.findViewById(R.id.shopping_list);
        Button btnEdit = result.findViewById(R.id.btn_edit);
        Button btnRemove = result.findViewById(R.id.btn_remove);

        ShoppingList item = getItem(position);
        text.setText(item.getText());

        btnEdit.setOnClickListener(v -> {
            // Placeholder for edit functionality
            editItem(position);
        });

        btnRemove.setOnClickListener(v -> {
            maybeRemoveItem(position);
        });

        return result;
    }

    private void maybeRemoveItem(int position) {
        ShoppingList item = getItem(position);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle(R.string.confirm);
        String fmt = getContext().getResources().getString(R.string.confirm_message, item.getText());
        builder.setMessage(fmt);
        builder.setPositiveButton(R.string.remove, (dialog, which) -> {
            int listId = item.getId();
            db.deleteList(listId);
            remove(item);
            notifyDataSetChanged();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void editItem(int position) {
        ShoppingList item = getItem(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.edit_list);

        // Configura o EditText
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(item.getText());
        builder.setView(input);

        // Configura os botões do diálogo
        builder.setPositiveButton("Save", (dialog, which) -> {
            String itemName = input.getText().toString();
            if (!itemName.isEmpty()) {
                item.setText(itemName);
                db.updateList(item.getId(), itemName);
                notifyDataSetChanged();
                Toast.makeText(getContext(), "Item atualizado com sucesso", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Nome do item é obrigatório", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
