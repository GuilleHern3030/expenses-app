package enel.dev.budgets.views.summary;

import android.app.Activity;


import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;
import enel.dev.budgets.utils.CustomLinearLayoutManager;
import enel.dev.budgets.utils.RecyclerViewNoScrollable;

public class SummaryView {

    private final Activity context;
    private final FrameLayout parent;
    private final boolean isIncomes;
    private final NumberFormat numberFormat;
    private final Coin preferedCoin;

    private final TextView totalView;


    private SummaryRecyclerView summaryList;
    private final RecyclerViewNoScrollable recyclerView;

    public SummaryView(final Activity context, final FrameLayout parent, final boolean isIncomes) {
        this.context = context;
        this.parent = parent;
        this.isIncomes = isIncomes;
        this.numberFormat = Preferences.decimalFormat(context);
        this.preferedCoin = Preferences.defaultCoin(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.fragment_summary_view, parent, false);
        ((TextView)view.findViewById(R.id.summary_view_title)).setText(isIncomes ? context.getString(R.string.incomes) : context.getString(R.string.expenses));
        ((TextView)view.findViewById(R.id.summary_view_total_text)).setText(isIncomes ? context.getString(R.string.transactions_description_incomes) : context.getString(R.string.transactions_description_expenses));
        parent.addView(view);

        recyclerView = view.findViewById(R.id.summary_view_list);
        totalView = view.findViewById(R.id.summary_view_total);

    }

    public SummaryView(final Activity context, final FrameLayout parent) {
        this.context = context;
        this.parent = parent;
        this.isIncomes = false;
        this.recyclerView = null;
        this.numberFormat = Preferences.decimalFormat(context);
        this.preferedCoin = Preferences.defaultCoin(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.fragment_summary_view, parent, false);
        view.findViewById(R.id.summary_view_title).setVisibility(View.GONE);
        view.findViewById(R.id.summary_view_list).setVisibility(View.GONE);
        parent.addView(view);

        totalView = view.findViewById(R.id.summary_view_total);

    }

    public void initialize(final Transactions transactions) {
        if (recyclerView != null) {
            recyclerView.removeAllViews();

            recyclerView.setLayoutManager(new CustomLinearLayoutManager(context));
            this.summaryList = new SummaryRecyclerView(transactions, numberFormat, preferedCoin);
            this.summaryList.setOnCategoryClick(category -> onItemClickListener.onCategoryClicked(category));
            recyclerView.setAdapter(summaryList);
            actualizeBalance();

        }
    }

    public void add(final Transaction transaction) {
        if (summaryList != null && transaction.isAnIncome() == isIncomes) {
            summaryList.add(transaction);
            actualizeBalance();
        }
    }

    public void remove(final Transaction transaction) {
        if (summaryList != null && transaction.isAnIncome() == isIncomes) {
            summaryList.remove(transaction);
            actualizeBalance();
            if (summaryList.getTransactions().size() == 0)
                hide();
        }
    }

    public void edit(final Transaction oldTransaction, final Transaction newTransaction) {
        if (newTransaction.isAnIncome() == isIncomes && oldTransaction.isAnIncome() == isIncomes) {
            if (summaryList != null) {
                summaryList.set(oldTransaction, newTransaction);
                actualizeBalance();
            }
        }
    }

    public void show() {
        if (summaryList.getTransactions().size() > 0) {
            parent.setVisibility(View.VISIBLE);
        }
    }

    public Transactions getTransactions() {
        return summaryList != null ? summaryList.getTransactions() : null;
    }

    public void actualizeBalance(final Money totalExpenses, final Money totalIncomes) {
        if (totalExpenses.getAmount() != 0 && totalIncomes.getAmount() != 0) {
            final Money total = totalIncomes.clone();
            total.add(-totalExpenses.getAmount());
            totalView.setText(total.toString(numberFormat));
            parent.setVisibility(View.VISIBLE);
        } else totalView.setText("0");
    }

    private void actualizeBalance() {
        totalView.setText(getTransactions().total(preferedCoin).toString(numberFormat));
    }

    public void hide() {
        parent.setVisibility(View.GONE);
    }

    public void setVisibility(final int v) {
        parent.setVisibility(v);
    }

    // Listener
    private onCategoryClickListener onItemClickListener;
    public interface onCategoryClickListener {
        void onCategoryClicked(final Category category);
    }
    public void setOnCategoryClick(onCategoryClickListener listener) {
        this.onItemClickListener = listener;
    }

}