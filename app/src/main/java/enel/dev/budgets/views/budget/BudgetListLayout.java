package enel.dev.budgets.views.budget;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.budget.Budget;
import enel.dev.budgets.objects.budget.Budgets;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transactions;

@SuppressLint("ViewConstructor")
public class BudgetListLayout {

    private final NumberFormat decimalFormat;
    private final String defaultCoinSymbol;
    private final Activity context;
    private final LinearLayout layout;
    private final FrameLayout totalFrame;

    public BudgetListLayout(final Activity context, final LinearLayout layout,final FrameLayout totalFrame, final NumberFormat decimalFormat, final String defaultCoinSymbol) {
        super();
        this.decimalFormat = decimalFormat;
        this.defaultCoinSymbol = defaultCoinSymbol;
        this.context = context;
        this.layout = layout;
        this.totalFrame = totalFrame;
    }

    @SuppressLint("ResourceAsColor")
    public void showContent(final Budgets budgets, final Transactions transactions) {
        layout.removeAllViews();

        if (budgets == null) {
            View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);
            view.findViewById(R.id.progressBar).setVisibility(GONE);
            TextView emptyText = view.findViewById(R.id.empty_text);
            emptyText.setText(context.getString(R.string.budgets_empty));
            emptyText.setVisibility(VISIBLE);
            layout.addView(view);
            totalFrame.setVisibility(GONE);
        }
        else if (budgets.size() > 0) for (Budget budget : budgets) {
            View view = LayoutInflater.from(context).inflate(R.layout.listview_budget, layout, false);

            FrameLayout categoryBackground = view.findViewById(R.id.category_container);
            ImageView categoryImage = view.findViewById(R.id.category_image);
            TextView categoryName = view.findViewById(R.id.category_name);

            categoryImage.setImageResource(budget.getCategory().getImage());
            categoryBackground.setBackgroundResource(budget.getCategory().getColor());
            categoryName.setText(budget.getCategory().getName());

            if (budget.getAmount() > 0) {
                totalFrame.setVisibility(VISIBLE);
                ProgressBar progressBar = view.findViewById(R.id.progressBar);
                TextView amountTextView = view.findViewById(R.id.amount);
                TextView amountTextView2 = view.findViewById(R.id.amount2);
                Money maxMoneyConfigured = new Money("", defaultCoinSymbol, budget.getAmount());
                Money moneyTransactioned = transactions.filterCategory(budget.getCategory().getName()).total(Preferences.defaultCoin(context));
                configureProgressBar(progressBar, amountTextView, amountTextView2, moneyTransactioned, maxMoneyConfigured);
            } else {
                view.findViewById(R.id.amount_container).setVisibility(GONE);
                view.findViewById(R.id.amount_editor_button).setVisibility(VISIBLE);
                totalFrame.setVisibility(GONE);
            }

            // Listener
            view.findViewById(R.id.frame).setOnClickListener(v -> onItemClickListener.budgetClicked(budgets.getId(), budget));
            view.findViewById(R.id.frame).setOnLongClickListener(v -> onItemClickListener.budgetLongClicked(budgets.getId(), budget));

            layout.addView(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);
            view.findViewById(R.id.progressBar).setVisibility(GONE);
            TextView emptyText = view.findViewById(R.id.empty_text);
            emptyText.setText(context.getString(R.string.budget_empty));
            emptyText.setVisibility(VISIBLE);
            layout.addView(view);
            totalFrame.setVisibility(GONE);
        }
    }

    public void showLoading() {
        totalFrame.setVisibility(GONE);
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

    @SuppressLint("SetTextI18n")
    private void configureProgressBar(
            final ProgressBar progressBar,
            final TextView textView,
            final TextView textView2,
            final Money amountTransactioned,
            final Money amountBudgeted) {

        textView.setText(getFormatedText(amountTransactioned, decimalFormat) + "   /");
        textView2.setText(getFormatedText(amountBudgeted, decimalFormat));
        
        progressBar.setProgress(100 - (int)(amountTransactioned.getAmount() * 100 / amountBudgeted.getAmount()), true);
    }

    private String getFormatedText(final Money money, final NumberFormat decimalFormat) {
        return money.getAmount() > 100000 && decimalFormat.withDecimals() ?
                money.toString(decimalFormat).split(String.valueOf(decimalFormat.decimalSeparator()))[0] :
                money.toString(decimalFormat);
    }

    // Listener
    private OnElementClickListener onItemClickListener;
    public interface OnElementClickListener {
        void budgetClicked(int id, Budget budget);
        boolean budgetLongClicked(int id, Budget budget);
    }
    public void setOnBudgetCLickListener(OnElementClickListener listener) {
        this.onItemClickListener = listener;
    } // */

}
