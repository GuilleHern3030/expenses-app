package enel.dev.budgets.views.budget;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.budget.Budget;
import enel.dev.budgets.objects.budget.Budgets;
import enel.dev.budgets.objects.money.Money;

@SuppressLint("ViewConstructor")
public class BudgetMenuListLayout {

    private final Context context;
    private final LinearLayout layout;

    public BudgetMenuListLayout(final Context context, final LinearLayout layout) {
        super();
        this.context = context;
        this.layout = layout;
    }

    @SuppressLint("ResourceAsColor")
    public void showContent(final ArrayList<String> budgetsList) {
        layout.removeAllViews();

        if (budgetsList.size() > 0) for (final String budgetsName : budgetsList) {
            View view = LayoutInflater.from(context).inflate(R.layout.listview_budget_menu, layout, false);

            TextView budgetName = view.findViewById(R.id.budget_name);
            budgetName.setText(budgetsName);

            // Listener
            view.findViewById(R.id.frame).setOnClickListener(v -> onItemClickListener.budgetClicked(budgetsName));
            view.findViewById(R.id.frame).setOnLongClickListener(v -> onItemClickListener.budgetLongClicked(budgetsName));

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
        void budgetClicked(String budgetsName);
        boolean budgetLongClicked(String budgetsName);
    }
    public void setOnBudgetCLickListener(OnElementClickListener listener) {
        this.onItemClickListener = listener;
    } // */

}
