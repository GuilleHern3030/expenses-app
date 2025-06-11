package enel.dev.budgets.views.summary;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;

@SuppressLint("ViewConstructor")
public class SummaryListLayout {

    private final NumberFormat decimalFormat;
    private final Context context;
    private final LinearLayout layout;
    private final boolean withBalance;
    private final boolean isIncomes;

    public SummaryListLayout(final Context context, final LinearLayout layout, NumberFormat decimalFormat) {
        super();
        this.decimalFormat = decimalFormat;
        this.context = context;
        this.layout = layout;
        this.withBalance = false;
        this.isIncomes = false;
    }

    public SummaryListLayout(final Context context, final LinearLayout layout, NumberFormat decimalFormat, final boolean isIncomes) {
        super();
        this.decimalFormat = decimalFormat;
        this.context = context;
        this.layout = layout;
        this.withBalance = true;
        this.isIncomes = isIncomes;
    }

    public void showContent(final LinearLayout layout, final Transactions transactions, final Money total) {
        layout.removeAllViews();
        final double totalAmount = total.getAmount();

        double maxPercent = 0;
        for (Transaction transaction : transactions) {
            final double percent = this.percent(transaction.getMoney().getAmount(), (int) totalAmount) + 1;
            maxPercent = Math.max(percent, maxPercent);
        }

        if (transactions.size() > 0) {

            if (withBalance) {
                final View container = LayoutInflater.from(context).inflate(R.layout.listview_summary_container, layout, false);
                final TextView tvTitle = container.findViewById(R.id.summary_view_title);
                final TextView tvSubTitle = container.findViewById(R.id.summary_view_total_text);
                final TextView tvTotalAmount = container.findViewById(R.id.summary_view_total);
                tvTitle.setText(isIncomes ? context.getString(R.string.incomes) : context.getString(R.string.expenses));
                tvSubTitle.setText(isIncomes ? context.getString(R.string.transactions_description_incomes) : context.getString(R.string.transactions_description_expenses));
                tvTotalAmount.setText(total.toString(decimalFormat));

                layout.addView(container);

                final LinearLayout linearLayout = container.findViewById(R.id.summary_view_list_container);
                for (Transaction transaction : transactions)
                    add(layout, linearLayout, transaction, (int) totalAmount, (int) maxPercent);
            } else
                for (Transaction transaction : transactions)
                    add(layout, layout, transaction, (int) totalAmount, (int) maxPercent);

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

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void add(final LinearLayout parent, final LinearLayout linearLayout, final Transaction transaction, final int totalAmount, final int maxPercent) {

        final View view = LayoutInflater.from(context).inflate(R.layout.listview_summary, parent, false);

        final double percent = this.percent(transaction.getMoney().getAmount(), totalAmount);

        final ImageView categoryImage = view.findViewById(R.id.summary_transaction_category_image);
        final FrameLayout categoryBackground = view.findViewById(R.id.summary_transaction_category_container);
        final FrameLayout bar = view.findViewById(R.id.bar);
        bar.setVisibility(View.GONE);

        final TextView tvCategoryName = view.findViewById(R.id.summary_transaction_category_name);
        final TextView tvAmount = view.findViewById(R.id.summary_transaction_amount);
        final TextView tvPercent = view.findViewById(R.id.percent);

        categoryBackground.setBackgroundResource(transaction.getCategory().getColor());
        categoryImage.setImageResource(transaction.getCategory().getImage());
        bar.setBackgroundResource(transaction.getCategory().getColor());

        tvCategoryName.setText(transaction.getCategory().getName());
        tvAmount.setText(transaction.getMoney().toString(this.decimalFormat));
        tvPercent.setText(String.format("%.1f", percent) + " %");

        LinearLayout frame = view.findViewById(R.id.frame);
        frame.setOnClickListener(view1 -> onItemClickListener.onCategoryClicked(transaction.id(), transaction.getCategory()));

        createBar(view.findViewById(R.id.bar_container), bar, (int) percent, maxPercent);

        linearLayout.addView(view);

    }

    public void showContent(final Transactions transactions, final Money total) {
        showContent(layout, transactions, total);
    }

    public void showContent(final View view) {
        layout.removeAllViews();
        layout.addView(view);
    }

    public void showContent(final View view, final int visibility) {
        showContent(view);
        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setVisibility(visibility);
            }
        });
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

    public LinearLayout getLayout() {
        return layout;
    }

    public void setVisibility(final int visibility) {
        layout.setVisibility(visibility);
    }

    public int getVisibility() {
        return layout.getVisibility();
    }

    public void hide() {
        setVisibility(GONE);
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    // Listener

    private double percent(final double amount, final int total) {
        return Math.min(amount * 100 / (double)total, 100.00);
    }

    // Listener
    private onCategoryClickListener onItemClickListener;
    public interface onCategoryClickListener {
        void onCategoryClicked(final int row, final Category category);
    }
    public void setOnCategoryClick(onCategoryClickListener listener) {
        this.onItemClickListener = listener;
    } // */

    private void createBar(final FrameLayout barContainer, final FrameLayout bar, final int barPercent, final int maxPercent) {
        // Usar un ViewTreeObserver para asegurarse de que el FrameLayout se ha renderizado
        barContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                // Quitar el listener para evitar que se llame múltiples veces
                barContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Obtener el ancho del FrameLayout
                int width = barContainer.getWidth();

                // Cambiar el ancho del FrameLayout programáticamente
                ViewGroup.LayoutParams params = bar.getLayoutParams();
                params.width = Math.max(barPercent, 8) * width / maxPercent; // Establecer el ancho en píxeles
                bar.setLayoutParams(params);
                bar.setVisibility(View.VISIBLE);
            }
        });
    }

}
