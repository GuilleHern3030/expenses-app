package enel.dev.budgets.views.summary;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import enel.dev.budgets.R;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;
import enel.dev.budgets.objects.transaction.TransactionsArray;
import enel.dev.budgets.views.home.TransactionsListLayout;

@SuppressLint("ViewConstructor")
@SuppressWarnings("unused")
public class TransactionsByDayListLayout {

    private Transactions transactions;

    private final NumberFormat decimalFormat;
    private final Context context;
    private final LinearLayout layout;

    public TransactionsByDayListLayout(final Context context, final LinearLayout layout, NumberFormat decimalFormat) {
        super();
        this.decimalFormat = decimalFormat;
        this.context = context;
        this.layout = layout;
    }

    public void set(final Transactions transactions) {
        this.transactions = transactions;
    }

    public void remove(final Transaction transaction) {
        transactions.remove(transaction);
    }

    public void edit(final Transaction oldTransaction, final Transaction newTransaction) {
        transactions.set(oldTransaction, newTransaction);
    }

    public void add(final Transaction transaction) {
        transactions.remove(transaction);
    }

    public Transactions getTransactions() {
        return transactions;
    }

    public Transactions getTransactions(final boolean incomes) {
        return incomes ? transactions.incomes() : transactions.expenses();
    }

    public void showContent(final LinearLayout layout, final TransactionsArray transactionsArray) {
        layout.removeAllViews();

        if (transactionsArray.size() > 0) for (int t = 0; t < transactionsArray.size(); t++) {
            final Transactions transactions = transactionsArray.get(t);
            final int i = t;

            View view = LayoutInflater.from(context).inflate(R.layout.listview_transactions_by_day, layout, false);

            final Date date = transactions.get(0).getDate();

            TextView tvDay = view.findViewById(R.id.tvDate_day);
            TextView tvMonth = view.findViewById(R.id.tvDate_month);
            TextView tvYear = view.findViewById(R.id.tvDate_year);

            tvDay.setText(date.day());
            tvMonth.setText(date.getMonthNameReduced(context));
            tvYear.setText(date.year());

            view.findViewById(R.id.transactionsListDay).setOnClickListener(v -> onItemClickListener.transactionDayClicked(date));

            TransactionsListLayout listOfTransactionsView = new TransactionsListLayout(context, view.findViewById(R.id.transactionsList), decimalFormat);
            listOfTransactionsView.showContent(transactions);
            listOfTransactionsView.setOnTransactionCLick(new TransactionsListLayout.OnElementClickListener() {
                @Override
                public void transactionClicked(int row, Transaction transaction) {
                    onItemClickListener.transactionClicked(i, row, transaction);
                }

                @Override
                public boolean transactionLongClicked(int row, Transaction transaction) {
                    onItemClickListener.transactionLongClicked(i, row, transaction);
                    return true;
                }

                @Override
                public void transactionPhoto(int row, Transaction transaction) {
                    onItemClickListener.transactionPhotoClicked(i, row, transaction);
                }
            });

            layout.addView(view);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);
            TextView emptyText = view.findViewById(R.id.empty_text);
            ProgressBar progressBar = view.findViewById(R.id.progressBar);
            emptyText.setText(context.getString(R.string.summary_empty));
            progressBar.setVisibility(GONE);
            emptyText.setVisibility(VISIBLE);
            layout.addView(view);
        }

    }

    public void showContent(final TransactionsArray transactionsArray) {
        showContent(layout, transactionsArray);
    }

    public void showContent(final View view) {
        layout.removeAllViews();
        if (view != null)
            layout.addView(view);
    }

    public LinearLayout getLayout() {
        return this.layout;
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

    public void setVisibility(final int visibility) {
        layout.setVisibility(visibility);
    }

    private OnDayClickListener onItemClickListener;
    public interface OnDayClickListener {
        void transactionClicked(int i, int row, Transaction transaction);
        void transactionPhotoClicked(int i, int row, Transaction transaction);
        void transactionLongClicked(int i, int row, Transaction transaction);
        void transactionDayClicked(final Date date);
    }
    public void setOnTransactionCLick(OnDayClickListener listener) {
        this.onItemClickListener = listener;
    }

}
