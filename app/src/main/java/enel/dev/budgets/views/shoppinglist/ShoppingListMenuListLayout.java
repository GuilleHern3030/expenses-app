package enel.dev.budgets.views.shoppinglist;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import enel.dev.budgets.R;

@SuppressLint("ViewConstructor")
public class ShoppingListMenuListLayout {

    private final Context context;
    private final LinearLayout layout;

    public ShoppingListMenuListLayout(final Context context, final LinearLayout layout) {
        super();
        this.context = context;
        this.layout = layout;
    }

    @SuppressLint("ResourceAsColor")
    public void showContent(final ArrayList<String> listOfLists) {
        layout.removeAllViews();

        if (listOfLists.size() > 0) for (final String shoppingList : listOfLists) {
            View view = LayoutInflater.from(context).inflate(R.layout.listview_budget_menu, layout, false);

            TextView budgetName = view.findViewById(R.id.budget_name);
            budgetName.setText(shoppingList);

            // Listener
            view.findViewById(R.id.frame).setOnClickListener(v -> onItemClickListener.listClicked(shoppingList));
            view.findViewById(R.id.frame).setOnLongClickListener(v -> onItemClickListener.listLongClicked(shoppingList));

            layout.addView(view);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);
            view.findViewById(R.id.progressBar).setVisibility(GONE);
            TextView emptyText = view.findViewById(R.id.empty_text);
            emptyText.setTextColor(ContextCompat.getColor(context, R.color.action_bar_foreground));
            emptyText.setText(context.getString(R.string.budgets_empty));
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
    private OnElementClickListener onItemClickListener;
    public interface OnElementClickListener {
        void listClicked(String listName);
        boolean listLongClicked(String listName);
    }
    public void setOnBudgetCLickListener(OnElementClickListener listener) {
        this.onItemClickListener = listener;
    } // */

}
