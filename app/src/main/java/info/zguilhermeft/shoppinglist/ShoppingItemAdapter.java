package info.zguilhermeft.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.compras.R;

import java.util.List;

public class ShoppingItemAdapter extends ArrayAdapter<ShoppingItem> {

    // Instância do helper do banco de dados para operações de CRUD.
    private DatabaseHelper db;

    // Construtor do adaptador que inicializa contexto, recurso de layout, lista de itens e o helper do banco de dados.
    public ShoppingItemAdapter(Context context, int resource, List<ShoppingItem> objects, DatabaseHelper db) {
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
            result = inflater.inflate(R.layout.shopping_item, parent, false);
        }
        // Encontra e configura o checkbox de cada item.
        CheckBox checkbox = result.findViewById(R.id.shopping_item);
        ShoppingItem item = getItem(position);
        checkbox.setText(item.getText());
        checkbox.setChecked(item.isChecked());

        // Botões para remover e editar cada item da lista.
        Button btnRemove = result.findViewById(R.id.btn_remove);
        Button btnEdit = result.findViewById(R.id.btn_edit);

        // Configura o listener para remover item.
        btnRemove.setOnClickListener(v -> maybeRemoveItem(position));

        // Configura o listener para editar item.
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditItemActivity.class);
            intent.putExtra("itemId", item.getId());
            // Assumindo que getContext() retorna a instância correta de ShoppingItemActivity.
            if (getContext() instanceof ShoppingItemActivity) {
                ((ShoppingItemActivity) getContext()).addItemActivityResultLauncher.launch(intent);
            } else {
                // Lança exceção se o contexto não for o esperado.
                throw new RuntimeException("The context used isn't ShoppingItemActivity");
            }
        });

        return result;
    }

    // Método para possivelmente remover um item, com confirmação.
    private void maybeRemoveItem(int position) {
        ShoppingItem item = getItem(position);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle(R.string.confirm);
        // Mensagem de confirmação dinâmica que inclui o texto do item.
        String fmt = getContext().getResources().getString(R.string.confirm_message_item, item.getText());
        builder.setMessage(fmt);
        builder.setPositiveButton(R.string.remove, (dialog, which) -> {
            // Remove item do banco de dados e atualiza a lista.
            int itemId = item.getId();
            db.deleteItem(itemId);
            remove(item);
            notifyDataSetChanged();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
}
