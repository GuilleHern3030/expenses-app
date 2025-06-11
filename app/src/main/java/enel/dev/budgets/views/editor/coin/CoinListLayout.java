package enel.dev.budgets.views.editor.coin;

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

import enel.dev.budgets.R;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.money.Balance;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transactions;

@SuppressLint("ViewConstructor")
public class CoinListLayout {

    private final Context context;
    private final LinearLayout layout;

    public CoinListLayout(final Context context, final LinearLayout layout) {
        super();
        this.context = context;
        this.layout = layout;
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void showContent(final Balance moneyList) {
        layout.removeAllViews();
        layout.setVisibility(VISIBLE);

        moneyList.removeDefaultCoin();

        if(moneyList.size() > 0) for (int i = 0; i < moneyList.size(); i++) {

            View view = LayoutInflater.from(context).inflate(R.layout.listview_balance, layout, false);
            final Money money = moneyList.get(i);
            final int row = i;

            // Nombre de la moneda
            TextView tvName = view.findViewById(R.id.coin_name);
            tvName.setText(money.getCoin().getName());

            // Simbolo de la moneda
            TextView tvSymbol = view.findViewById(R.id.coin_amount);
            tvSymbol.setText(money.getCoin().getSymbol());

            // Icon
            FrameLayout categoryBackground = view.findViewById(R.id.coin_image_container);
            categoryBackground.setBackgroundResource(R.drawable.rounded_shape_loading);
            ImageView categoryImage = view.findViewById(R.id.coin_image);
            categoryImage.setImageResource(R.drawable.icon_casino);

            // Listener
            ConstraintLayout frame = view.findViewById(R.id.balance_container);
            frame.setOnClickListener(v -> onItemClickListener.balanceClicked(row, money));
            frame.setOnLongClickListener(v -> onItemClickListener.balanceLongClicked(row, money));

            layout.addView(view);

        }
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


    // Listener
    private OnElementClickListener onItemClickListener;
    public interface OnElementClickListener {
        void balanceClicked(int row, Money balance);
        boolean balanceLongClicked(int row, Money balance);
    }
    public void setOnBalanceCLick(OnElementClickListener listener) {
        this.onItemClickListener = listener;
    } //

}
