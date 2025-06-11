package enel.dev.budgets.views.home;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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
public class TransactionsListLayout {

    private final NumberFormat decimalFormat;
    private final Context context;
    private final LinearLayout layout;

    public TransactionsListLayout(final Context context, final LinearLayout layout, NumberFormat decimalFormat) {
        super();
        this.decimalFormat = decimalFormat;
        this.context = context;
        this.layout = layout;
    }

    public void showContent(final LinearLayout layout, final Transactions transactions) {
        layout.removeAllViews();

        if (transactions.size() > 0) {
            for (int i = 0; i < transactions.size(); i++)
                addContent(layout, i, transactions.get(i));
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

    public void showContent(final Transactions transactions) {
        showContent(layout, transactions);
    }

    public void showContent(final View view) {
        try {
            layout.removeAllViews();
            layout.addView(view);
        } catch (Exception ignored) { }
    }

    public LinearLayout getLayout() {
        return layout;
    }

    private void addContent(final LinearLayout layout, final int index, final Transaction transaction) {

        View view = LayoutInflater.from(context).inflate(R.layout.listview_transactions, layout, false);

        LinearLayout frame = view.findViewById(R.id.transaction_background);
        frame.setBackgroundResource(transaction.isAnIncome() ? R.color.income_background : R.color.expense_background);

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

        // Listener
        frame.setOnClickListener(v -> onItemClickListener.transactionClicked(index, transaction));
        frame.setOnLongClickListener(v -> onItemClickListener.transactionLongClicked(index, transaction));

        ImageView photoButton = view.findViewById(R.id.transaction_image);
        if (transaction.getPhotoUri().length() > 0) {
            photoButton.setVisibility(VISIBLE);
            photoButton.setOnClickListener(v -> onItemClickListener.transactionPhoto(index, transaction));
        }

        layout.addView(view);
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
        void transactionClicked(int index, Transaction transaction);
        boolean transactionLongClicked(int index, Transaction transaction);
        void transactionPhoto(int index, Transaction transaction);
    }
    public void setOnTransactionCLick(OnElementClickListener listener) {
        this.onItemClickListener = listener;
    } // */

}
