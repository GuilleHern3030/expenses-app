package enel.dev.budgets.views.home;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.reserve.Reserves;
import enel.dev.budgets.objects.reserve.Reserve;

/*
    Listado de reservas declaradas por el usuario
 */

public class ReserveList {

    private final NumberFormat decimalFormat;
    private final Context context;
    private final LinearLayout layout;

    public ReserveList(Context context, final LinearLayout layout, NumberFormat decimalFormat) {
        super();
        this.decimalFormat = decimalFormat;
        this.context = context;
        this.layout = layout;
    }

    public void showContent(Reserves debts) {
        layout.removeAllViews();

        if (debts.size() > 0) for (int i = 0; i < debts.size(); i++) {
            final Reserve reserve = debts.get(i);
            final int index = i;

            View view = LayoutInflater.from(context).inflate(R.layout.listview_reserve, layout, false);


            // Listener
            LinearLayout frame = view.findViewById(R.id.debt_container);
            frame.setOnClickListener(v -> onItemClickListener.reserveClicked(index, reserve));
            frame.setOnLongClickListener(v -> onItemClickListener.reserveLongClicked(index, reserve));

            // Nombre de la reserva
            TextView tvName = view.findViewById(R.id.reserve_name);
            tvName.setText(reserve.getName());

            // Cantidad de reserva
            TextView tvDebt = view.findViewById(R.id.reserve_amount);
            String amount = Money.toString("$", reserve.getAmount(), this.decimalFormat);
            tvDebt.setText(amount);

            layout.addView(view);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);
            TextView emptyText = view.findViewById(R.id.empty_text);
            ProgressBar progressBar = view.findViewById(R.id.progressBar);
            emptyText.setText(context.getString(R.string.reserves_empty));
            progressBar.setVisibility(GONE);
            emptyText.setVisibility(VISIBLE);
            emptyText.setOnClickListener(view1 -> onItemClickListener.emptyReserveClicked());
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
        void reserveClicked(int row, Reserve debt);
        boolean reserveLongClicked(int row, Reserve debt);
        void emptyReserveClicked();
    }
    public void setOnReserveClick(OnElementClickListener listener) {
        this.onItemClickListener = listener;
    }

}