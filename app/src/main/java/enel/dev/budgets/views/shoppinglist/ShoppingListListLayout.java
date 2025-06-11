package enel.dev.budgets.views.shoppinglist;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.shoppinglist.Item;
import enel.dev.budgets.objects.shoppinglist.ShoppingList;

@SuppressLint("ViewConstructor")
public class ShoppingListListLayout {

    private final Activity context;
    private final LinearLayout layout;

    public ShoppingListListLayout(final Activity context, final LinearLayout layout) {
        super();
        this.context = context;
        this.layout = layout;
    }

    @SuppressLint("ResourceAsColor")
    public void showContent(final ShoppingList itemList) {
        layout.removeAllViews();

        if (itemList == null) {
            View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);
            view.findViewById(R.id.progressBar).setVisibility(GONE);
            TextView emptyText = view.findViewById(R.id.empty_text);
            emptyText.setText(context.getString(R.string.shopping_lists_empty));
            emptyText.setVisibility(VISIBLE);
            layout.addView(view);
        }
        else if (itemList.size() > 0) for (int i = 0; i < itemList.size(); i++) {
            final Item item = itemList.get(i);
            final int id = i;

            View view = LayoutInflater.from(context).inflate(R.layout.listview_shoppinglist, layout, false);

            final FrameLayout strikethrough = view.findViewById(R.id.item_strikethrough);
            final TextView tvName = view.findViewById(R.id.item_name_text);
            final CheckBox checkBox = view.findViewById(R.id.checkbox);

            tvName.setText(item.getName());
            checkBox.setChecked(item.isCompleted());
            strikethrough.setVisibility(item.isCompleted() ? VISIBLE : GONE);
            if (item.isCompleted()) tvName.setPaintFlags(tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // tachar texto
            else tvName.setPaintFlags(tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)); // quitar tachado

            // Listener
            view.findViewById(R.id.frame).setOnClickListener(v -> onItemClickListener.itemClicked(id, item));
            view.findViewById(R.id.frame).setOnLongClickListener(v -> onItemClickListener.itemLongClicked(id, item));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                onItemClickListener.itemCheckClicked(id, item, isChecked);
                strikethrough.setVisibility(isChecked ? VISIBLE : GONE);
                if (isChecked) tvName.setPaintFlags(tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // tachar texto
                else tvName.setPaintFlags(tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)); // quitar tachado
            });

            layout.addView(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);
            view.findViewById(R.id.progressBar).setVisibility(GONE);
            TextView emptyText = view.findViewById(R.id.empty_text);
            emptyText.setText(context.getString(R.string.shopping_list_empty));
            emptyText.setVisibility(VISIBLE);
            layout.addView(view);
        }
    }

    public void showLoading() {
        layout.removeAllViews();
        View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView emptyText = view.findViewById(R.id.empty_text);
        progressBar.setVisibility(VISIBLE);
        emptyText.setVisibility(GONE);
        layout.addView(view);
    }

    public void setVisibility(int visibility) {
        layout.setVisibility(visibility);
    }

    // Listener
    private OnItemCLickListener onItemClickListener;
    public interface OnItemCLickListener {
        void itemClicked(final int i, final Item listItem);
        void itemCheckClicked(final int i, final Item listItem, final boolean isChecked);
        boolean itemLongClicked(final int i, final Item listItem);
    }
    public void setOnItemCLickListener(OnItemCLickListener listener) {
        this.onItemClickListener = listener;
    } // */

}
