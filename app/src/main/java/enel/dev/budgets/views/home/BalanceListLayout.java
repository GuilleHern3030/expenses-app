package enel.dev.budgets.views.home;

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

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.money.Balance;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transactions;

@SuppressLint("ViewConstructor")
public class BalanceListLayout {

    private final NumberFormat decimalFormat;
    private final Context context;
    private final LinearLayout layout;
    private Coin defaultCoin;

    public BalanceListLayout(final Context context, final LinearLayout layout, final NumberFormat decimalFormat, final Coin defaultCoin) {
        super();
        this.decimalFormat = decimalFormat;
        this.context = context;
        this.layout = layout;
        this.defaultCoin = defaultCoin;
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void showContent(final Balance moneyList, final Transactions transactions) {

        if (moneyList.size() > 1) {

            layout.removeAllViews();
            layout.setVisibility(VISIBLE);

            for (int i = 0; i < moneyList.size(); i++) {

                View view = LayoutInflater.from(context).inflate(R.layout.listview_balance, layout, false);

                final Money money = new Money(moneyList.get(i).getCoin(), transactions.incomes().total(moneyList.get(i).getCoin()).getAmount() - transactions.expenses().total(moneyList.get(i).getCoin()).getAmount());
                final int row = i;

                // Nombre de la moneda
                TextView tvName = view.findViewById(R.id.coin_name);
                tvName.setText(money.getCoin().getName());

                // Cantidad de monedas
                TextView tvAmount = view.findViewById(R.id.coin_amount);
                tvAmount.setText(money.toString());

                // Icon
                FrameLayout categoryBackground = view.findViewById(R.id.coin_image_container);
                categoryBackground.setBackgroundResource(R.drawable.rounded_shape_dark);
                ImageView categoryImage = view.findViewById(R.id.coin_image);
                categoryImage.setImageResource(row == 0 ? R.drawable.efectivo_white : R.drawable.change_white);

                // Listener
                ConstraintLayout frame = view.findViewById(R.id.balance_container);
                frame.setOnClickListener(v -> onItemClickListener.balanceClicked(row, money));
                if (money.getCoin().getName().equals(defaultCoin.getName())) {
                    frame.setBackgroundResource(R.color.income_background);
                    tvName.setTextColor(ContextCompat.getColor(context, R.color.black));
                    tvAmount.setTextColor(ContextCompat.getColor(context, R.color.black));
                }

                layout.addView(view);
            }
        } else showContent(transactions);
    }

    public void showContent(final Transactions transactions) {
        layout.removeAllViews();
        layout.setVisibility(VISIBLE);

        View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView text = view.findViewById(R.id.empty_text);
        progressBar.setVisibility(GONE);

        if (transactions.size() > 0) {
            final Money total = new Money(defaultCoin, transactions.incomes().total(defaultCoin).getAmount() - transactions.expenses().total(defaultCoin).getAmount());
            text.setVisibility(VISIBLE);
            text.setText(total.toString(decimalFormat));
            text.setBackgroundResource(R.color.cardview_background);
            //text.setTextColor(preferedCoinTotal.getAmount() >= 0 ? ContextCompat.getColor(context, R.color.income_text) : ContextCompat.getColor(context, R.color.expense_text));
        } else {
            text.setText(context.getString(R.string.balance_empty));
        }

        layout.addView(view);
    }

    public void showLoading() {
        layout.removeAllViews();
        layout.setVisibility(VISIBLE);
        View view = LayoutInflater.from(context).inflate(R.layout.listlayout_content, layout, false);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView emptyText = view.findViewById(R.id.empty_text);
        progressBar.setVisibility(VISIBLE);
        emptyText.setVisibility(GONE);
        layout.addView(view);
    }

    public void setDefaultCoin(final Coin coin) {
        this.defaultCoin = coin;
    }


    // Listener
    private OnElementClickListener onItemClickListener;
    public interface OnElementClickListener {
        void balanceClicked(int row, Money balance);
    }
    public void setOnBalanceCLick(OnElementClickListener listener) {
        this.onItemClickListener = listener;
    } //

}
