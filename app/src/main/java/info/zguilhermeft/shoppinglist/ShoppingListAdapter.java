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
    // Instância do helper do banco de dados para operações de CRUD.
    private DatabaseHelper db;

    // Construtor que inicializa o contexto, o recurso de layout, os objetos e o database helper.
    public ShoppingListAdapter(Context context, int resource, List<ShoppingList> objects, DatabaseHelper db) {
        super(context, resource, objects);
        this.db = db;
    }

    // Método para criar ou reutilizar uma View para cada item da lista.
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result = convertView;
        // Se não há uma View reciclável, infla uma nova.
        if (result == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            result = inflater.inflate(R.layout.shopping_list, parent, false);
        }

        // Configura o texto e botões de cada item na lista.
        TextView text = result.findViewById(R.id.shopping_list);
        Button btnEdit = result.findViewById(R.id.btn_edit);
        Button btnRemove = result.findViewById(R.id.btn_remove);

        ShoppingList item = getItem(position);
        text.setText(item.getText());

        // Listener para edição de item.
        btnEdit.setOnClickListener(v -> {
            editItem(position);
        });

        // Listener para remoção de item com confirmação.
        btnRemove.setOnClickListener(v -> {
            maybeRemoveItem(position);
        });

        return result;
    }

    // Método para confirmar e remover um item da lista de compras.
    private void maybeRemoveItem(int position) {
        ShoppingList item = getItem(position);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle(R.string.confirm);
        // Mensagem de confirmação inclui o nome do item a ser removido.
        String fmt = getContext().getResources().getString(R.string.confirm_message, item.getText());
        builder.setMessage(fmt);
        builder.setPositiveButton(R.string.remove, (dialog, which) -> {
            int listId = item.getId();
            db.deleteList(listId); // Remove o item do banco de dados.
            remove(item); // Remove o item da lista atual.
            notifyDataSetChanged(); // Notifica que os dados mudaram.
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    // Método para editar um item da lista de compras.
    private void editItem(int position) {
        ShoppingList item = getItem(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.edit_list);

        // Configura o EditText para entrada do novo nome da lista.
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(item.getText());
        builder.setView(input);

        // Configura botões para salvar ou cancelar a edição.
        builder.setPositiveButton("Save", (dialog, which) -> {
            String itemName = input.getText().toString();
            if (!itemName.isEmpty()) {
                item.setText(itemName); // Atualiza o texto do item.
                db.updateList(item.getId(), itemName); // Atualiza o item no banco de dados.
                notifyDataSetChanged(); // Notifica que os dados mudaram.
                Toast.makeText(getContext(), "Item atualizado com sucesso", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Nome do item é obrigatório", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
