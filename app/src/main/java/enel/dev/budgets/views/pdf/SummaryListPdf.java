package enel.dev.budgets.views.pdf;

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
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;

@SuppressLint("ViewConstructor")
public class SummaryListPdf {

    private final NumberFormat decimalFormat;
    private final Context context;
    private final LinearLayout layout;

    public SummaryListPdf(final Context context, final LinearLayout layout, NumberFormat decimalFormat) {
        super();
        this.decimalFormat = decimalFormat;
        this.context = context;
        this.layout = layout;
    }

    @SuppressLint({"ResourceAsColor", "DefaultLocale", "SetTextI18n"})
    public void showContent(final Transactions transactions, final double total) {
        layout.removeAllViews();

        double maxPercent = 0;
        for (Transaction transaction : transactions) {
            final double percent = this.percent(transaction.getMoney().getAmount(), (int) total) + 1;
            maxPercent = Math.max(percent, maxPercent);
        }

        if (transactions.size() > 0) for (Transaction transaction : transactions) {
            View view = LayoutInflater.from(context).inflate(R.layout.listview_pdf_summary, layout, false);

            final double percent = this.percent(transaction.getMoney().getAmount(), (int) total);

            ImageView categoryImage = view.findViewById(R.id.summary_transaction_category_image);
            FrameLayout categoryBackground = view.findViewById(R.id.summary_transaction_category_container);
            FrameLayout bar = view.findViewById(R.id.bar);
            bar.setVisibility(View.GONE);
            TextView tvCategoryName = view.findViewById(R.id.summary_transaction_category_name);
            TextView tvAmount = view.findViewById(R.id.summary_transaction_amount);
            TextView tvPercent = view.findViewById(R.id.percent);

            categoryBackground.setBackgroundResource(transaction.getCategory().getColor());
            categoryImage.setImageResource(transaction.getCategory().getImage());
            bar.setBackgroundResource(transaction.getCategory().getColor());

            tvCategoryName.setText(transaction.getCategory().getName());
            tvAmount.setText(transaction.getMoney().toString(this.decimalFormat));
            tvPercent.setText(String.format("%.1f", percent) + " %");

            LinearLayout frame = view.findViewById(R.id.frame);

            createBar(view.findViewById(R.id.bar_container), bar, (int) percent, (int) maxPercent);

            layout.addView(view);

        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);
            TextView emptyText = view.findViewById(R.id.empty_text);
            ProgressBar progressBar = view.findViewById(R.id.progressBar);
            emptyText.setText(context.getString(R.string.summary_empty));
            progressBar.setVisibility(GONE);
            emptyText.setVisibility(VISIBLE);
            layout.addView(view);
        }

    }

    private double percent(final double amount, final int total) {
        return Math.min(amount * 100 / (double)total, 100.00);
    }

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
