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

    private DatabaseHelper db;

    public ShoppingItemAdapter(Context context, int resource, List<ShoppingItem> objects, DatabaseHelper db) {
        super(context, resource, objects);
        this.db = db;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result = convertView;
        if (result == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            result = inflater.inflate(R.layout.shopping_item, parent, false);
        }
        CheckBox checkbox = result.findViewById(R.id.shopping_item);
        ShoppingItem item = getItem(position);
        checkbox.setText(item.getText());
        checkbox.setChecked(item.isChecked());

        Button btnRemove = result.findViewById(R.id.btn_remove);
        Button btnEdit = result.findViewById(R.id.btn_edit);

        btnRemove.setOnClickListener(v -> maybeRemoveItem(position));

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditItemActivity.class);
            intent.putExtra("itemId", item.getId());
            // Assumindo que getContext() retorna a instância correta de ShoppingItemActivity
            if (getContext() instanceof ShoppingItemActivity) {
                ((ShoppingItemActivity) getContext()).addItemActivityResultLauncher.launch(intent);
            } else {
                throw new RuntimeException("The context used isn't ShoppingItemActivity");
            }
        });

        return result;
    }

    private void maybeRemoveItem(int position) {
        ShoppingItem item = getItem(position);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle(R.string.confirm);
        String fmt = getContext().getResources().getString(R.string.confirm_message_item, item.getText());
        builder.setMessage(fmt);
        builder.setPositiveButton(R.string.remove, (dialog, which) -> {
            int itemId = item.getId();
            db.deleteItem(itemId);
            remove(item);
            notifyDataSetChanged();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
}
