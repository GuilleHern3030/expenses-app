package enel.dev.budgets.views.editor.coin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.money.Balance;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.utils.SnackBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CoinDeleter#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CoinDeleter extends CoinContext {

    public CoinDeleter() {
        // Required empty public constructor
    }

    private Coin coin;

    public static CoinDeleter newInstance(Coin coin) {
        CoinDeleter fragment = new CoinDeleter();
        Bundle args = new Bundle();
        args.putString("coinname", coin.getName());
        args.putString("coinsymbol", coin.getSymbol());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            final String coinName = getArguments().getString("coinname");
            final String coinSymbol = getArguments().getString("coinsymbol");
            coin = new Coin(coinName, coinSymbol);
        } else coin = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coin_delete, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView = view.findViewById(R.id.coin_name);
        textView.setText(coin.getName() + " (" + coin.getSymbol() + ")");

        view.findViewById(R.id.bCancel).setOnClickListener(v -> closeFragment());
        view.findViewById(R.id.bAccept).setOnClickListener(v -> resultCoinDelete(coin));

    }
}