package enel.dev.budgets.views.home;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.objects.debt.Debt;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.debt.Debts;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transaction;

/*
    Listado de deudas declaradas por el usuario
 */

public class DebtList {

    private final NumberFormat decimalFormat;
    private final Context context;
    private final LinearLayout layout;

    public DebtList(Context context, final LinearLayout layout, NumberFormat decimalFormat) {
        super();
        this.decimalFormat = decimalFormat;
        this.context = context;
        this.layout = layout;
    }

    public void showContent(Debts debts) {
        layout.removeAllViews();

        if (debts.size() > 0) for (int i = 0; i < debts.size(); i++) {
            final Debt debt = debts.get(i);
            final int index = i;

            View view = LayoutInflater.from(context).inflate(R.layout.listview_debt, layout, false);


            // Listener
            LinearLayout frame = view.findViewById(R.id.debt_container);
            frame.setOnClickListener(v -> onItemClickListener.debtClicked(index, debt));
            frame.setOnLongClickListener(v -> onItemClickListener.debtLongClicked(index, debt));

            // Nombre del prestamista
            TextView tvName = view.findViewById(R.id.lender_name);
            tvName.setText(debt.getLender());

            // Descripcion
            TextView tvDescription = view.findViewById(R.id.debt_description_text);
            if (debt.getDescription().isEmpty()) tvDescription.setVisibility(GONE);
            else tvDescription.setText(debt.getDescription());

            // Cantidad de deuda
            TextView tvDebt = view.findViewById(R.id.debt_amount);
            tvDebt.setText(debt.getMoney().toString(this.decimalFormat));

            layout.addView(view);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);
            TextView emptyText = view.findViewById(R.id.empty_text);
            ProgressBar progressBar = view.findViewById(R.id.progressBar);
            emptyText.setText(context.getString(R.string.debts_empty));
            progressBar.setVisibility(GONE);
            emptyText.setVisibility(VISIBLE);
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
        void debtClicked(int row, Debt debt);
        boolean debtLongClicked(int row, Debt debt);
    }
    public void setOnDebtCLick(OnElementClickListener listener) {
        this.onItemClickListener = listener;
    }

}