package enel.dev.budgets.views.summary;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;

public class SummaryRecyclerView extends RecyclerView.Adapter<SummaryRecyclerView.ViewHolder> {
    private final Transactions mData;
    private final NumberFormat decimalFormat;
    private final Coin preferedCoin;
    private double maxPercent = 0;
    private double total;

    public SummaryRecyclerView(final Transactions data, final NumberFormat decimalFormat, final Coin preferedCoin) {
        this.mData = data;
        this.decimalFormat = decimalFormat;
        this.preferedCoin = preferedCoin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_summary, parent, false);

        actualizeMaxPercent();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int row) {
        final Transaction category = mData.get(row);

        final double percent = this.percent(category.getMoney().getAmount(), (int) total);

        @SuppressLint("DefaultLocale") final String percentText = String.format("%.1f", percent) + " %";

        holder.bar.setVisibility(View.GONE);

        holder.categoryBackground.setBackgroundResource(category.getCategory().getColor());
        holder.categoryImage.setImageResource(category.getCategory().getImage());
        holder.bar.setBackgroundResource(category.getCategory().getColor());

        holder.tvCategoryName.setText(category.getCategory().getName());
        holder.tvAmount.setText(category.getMoney().toString(this.decimalFormat));
        holder.tvPercent.setText(percentText);

        holder.frame.setOnClickListener(view1 -> onItemClickListener.onCategoryClicked(category.getCategory()));

        createBar(holder.barContainer, holder.bar, (int) percent, (int) maxPercent);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    // Agregar un nuevo ítem al final de la lista
    public void add(Transaction transaction) {
        final int index = mData.indexOf(transaction.getCategory());
        if (index >= 0 && index < mData.size()) {
            final double amount = transaction.getMoney().getAmount();
            mData.get(index).addMoney(amount);
            actualizeMaxPercent();
            notifyItemChanged(index);
        } else {
            mData.add(transaction);
            actualizeMaxPercent();
            notifyItemInserted(index);
        }
    }

    public void set(final Transaction oldCategory, final Transaction category) {
        final int index = mData.indexOf(oldCategory.getCategory());
        if (oldCategory.getMoney().getAmount() != category.getMoney().getAmount()) {
            if (index >= 0 && index < mData.size()) {
                Transaction existTransaction = mData.get(index);
                existTransaction.addMoney(-oldCategory.getMoney().getAmount());
                existTransaction.addMoney(category.getMoney().getAmount());
                mData.set(index, existTransaction);
                actualizeMaxPercent();
                notifyItemChanged(index);
            }
        }
    }

    // Eliminar un ítem en una posición específica
    public void remove(Transaction category) {
        final int index = mData.indexOf(category.getCategory());
        if (index >= 0 && index < mData.size()) {
            Transaction existTransaction = mData.get(index);
            existTransaction.addMoney(-category.getMoney().getAmount());
            if (existTransaction.getMoney().getAmount() <= 0) {
                mData.remove(index);
                actualizeMaxPercent();
                notifyItemRemoved(index);
            } else {
                mData.set(index, existTransaction);
                actualizeMaxPercent();
                notifyItemChanged(index);
            }
        }
    }

    public Transactions getTransactions() {
        return mData;
    }

    // Cargar los view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImage;
        FrameLayout categoryBackground;
        FrameLayout bar;
        FrameLayout barContainer;
        TextView tvCategoryName;
        TextView tvAmount;
        TextView tvPercent;
        LinearLayout frame;

        public ViewHolder(View view) {
            super(view);
            categoryImage = view.findViewById(R.id.summary_transaction_category_image);
            categoryBackground = view.findViewById(R.id.summary_transaction_category_container);
            bar = view.findViewById(R.id.bar);
            tvCategoryName = view.findViewById(R.id.summary_transaction_category_name);
            tvAmount = view.findViewById(R.id.summary_transaction_amount);
            tvPercent = view.findViewById(R.id.percent);
            frame = view.findViewById(R.id.frame);
            barContainer = view.findViewById(R.id.bar_container);
        }
    }

    // Listener
    private onCategoryClickListener onItemClickListener;
    public interface onCategoryClickListener {
        void onCategoryClicked(final Category category);
    }
    public void setOnCategoryClick(onCategoryClickListener listener) {
        this.onItemClickListener = listener;
    }

    private double percent(final double amount, final int total) {
        return Math.min(amount * 100 / (double)total, 100.00);
    }

    private void actualizeMaxPercent() {
        total = mData.total(preferedCoin).getAmount();
        for (Transaction transaction : mData) {
            final double percent = this.percent(transaction.getMoney().getAmount(), (int) total) + 1;
            maxPercent = Math.max(percent, maxPercent);
        }
    }

    private void createBar(final FrameLayout barContainer, final FrameLayout bar, final int barPercent, final int maxPercent) {
        // Usar un ViewTreeObserver para asegurarse de que el FrameLayout se ha renderizado
        barContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                try {
                    // Quitar el listener para evitar que se llame múltiples veces
                    barContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // Obtener el ancho del FrameLayout
                    int width = barContainer.getWidth();

                    // Cambiar el ancho del FrameLayout programáticamente
                    ViewGroup.LayoutParams params = bar.getLayoutParams();
                    params.width = Math.max(barPercent, 8) * width / maxPercent; // Establecer el ancho en píxeles
                    bar.setLayoutParams(params);
                    bar.setVisibility(View.VISIBLE);
                } catch (ArithmeticException ignored) { }
            }
        });
    }
}
