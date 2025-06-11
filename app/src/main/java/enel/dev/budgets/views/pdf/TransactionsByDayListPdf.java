package enel.dev.budgets.views.pdf;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
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

@SuppressLint("ViewConstructor")
public class TransactionsByDayListPdf {

    private Transactions transactions;

    private final NumberFormat decimalFormat;
    private final Context context;
    private final LinearLayout layout;

    public TransactionsByDayListPdf(final Context context, final LinearLayout layout, NumberFormat decimalFormat) {
        super();
        this.decimalFormat = decimalFormat;
        this.context = context;
        this.layout = layout;
    }

    public TransactionsByDayListPdf(final Context context, NumberFormat decimalFormat) {
        super();
        this.decimalFormat = decimalFormat;
        this.context = context;
        this.layout = null;
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

    @SuppressLint("ResourceAsColor")
    public void showContent(final LinearLayout layout, final TransactionsArray transactionsArray) {
        layout.removeAllViews();

        if (transactionsArray.size() > 0) for (int t = 0; t < transactionsArray.size(); t++) {
            final Transactions transactions = transactionsArray.get(t);
            final int i = t;

            View view = LayoutInflater.from(context).inflate(R.layout.listview_pdf_transactions_by_day, layout, false);

            final Date date = transactions.get(0).getDate();

            TextView tvDay = view.findViewById(R.id.tvDate_day);
            TextView tvMonth = view.findViewById(R.id.tvDate_month);
            TextView tvYear = view.findViewById(R.id.tvDate_year);

            tvDay.setText(date.day());
            tvMonth.setText(date.getMonthNameReduced(context));
            tvYear.setText(date.year());

            TransactionsListPdf listOfTransactionsView = new TransactionsListPdf(context, view.findViewById(R.id.transactionsList), decimalFormat);
            listOfTransactionsView.showContent(transactions);

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
        if (layout != null)
            showContent(layout, transactionsArray);
    }

}
