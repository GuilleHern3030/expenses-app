package enel.dev.budgets.views.pdf;

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

import enel.dev.budgets.R;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;

@SuppressLint("ViewConstructor")
public class TransactionsListPdf {

    private final NumberFormat decimalFormat;
    private final Context context;
    private final LinearLayout layout;

    public TransactionsListPdf(final Context context, final LinearLayout layout, NumberFormat decimalFormat) {
        super();
        this.decimalFormat = decimalFormat;
        this.context = context;
        this.layout = layout;
    }

    public void showContent(Transactions transactions) {
        layout.removeAllViews();

        if (transactions.size() > 0) for (int i = 0; i < transactions.size(); i++) {
            final Transaction transaction = transactions.get(i);

            View view = LayoutInflater.from(context).inflate(R.layout.listview_pdf_transactions, layout, false);

            LinearLayout frame = view.findViewById(R.id.transaction_background);
            frame.setBackgroundResource(transaction.isAnIncome() ? R.color.pdf_red : R.color.pdf_green);

            FrameLayout categoryBackground = view.findViewById(R.id.transaction_category_container);
            categoryBackground.setBackgroundResource(transaction.getCategory().getColor());

            ImageView categoryImage = view.findViewById(R.id.transaction_category_image);
            categoryImage.setImageResource(transaction.getCategory().getImage());

            TextView categoryName = view.findViewById(R.id.transaction_category_name);
            categoryName.setText(transaction.getCategory().getName());
            categoryName.setTextColor(transaction.isAnIncome() ? ContextCompat.getColor(context, R.color.income_text) : ContextCompat.getColor(context, R.color.expense_text));

            TextView description = view.findViewById(R.id.transaction_description);
            description.setText(transaction.getDescription());
            description.setTextColor(transaction.isAnIncome() ? ContextCompat.getColor(context, R.color.income_description) : ContextCompat.getColor(context, R.color.expense_description));

            TextView amount = view.findViewById(R.id.transaction_amount);
            amount.setText(transaction.getMoney().toString(decimalFormat));
            amount.setTextColor(transaction.isAnIncome() ? ContextCompat.getColor(context, R.color.income_text) : ContextCompat.getColor(context, R.color.expense_text));

            layout.addView(view);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);
            TextView emptyText = view.findViewById(R.id.empty_text);
            ProgressBar progressBar = view.findViewById(R.id.progressBar);
            emptyText.setText(context.getString(R.string.transactions_empty));
            progressBar.setVisibility(GONE);
            emptyText.setVisibility(VISIBLE);
            layout.addView(view);
        }

    }

    public void setVisibility(int visibility) {
        layout.setVisibility(visibility);
    }

}
