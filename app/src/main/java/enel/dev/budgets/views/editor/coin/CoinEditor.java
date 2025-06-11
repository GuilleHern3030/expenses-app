package enel.dev.budgets.views.editor.coin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.money.Balance;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.utils.SnackBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CoinEditor#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CoinEditor extends CoinContext {

    public CoinEditor() {
        // Required empty public constructor
    }

    private Coin coin;
    private Balance coins;

    public static CoinEditor newInstance() {
        return new CoinEditor();
    }

    public static CoinEditor newInstance(Coin coin) {
        CoinEditor fragment = new CoinEditor();
        Bundle args = new Bundle();
        args.putString("coinname", coin.getName());
        args.putString("coinsymbol", coin.getSymbol());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        coins = Controller.balances(requireActivity()).get();
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
        return inflater.inflate(R.layout.fragment_coin_creator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText etCoinName = view.findViewById(R.id.etCoinName);
        final EditText etCoinSymbol = view.findViewById(R.id.etCoinSymbol);

        if (coin != null) {
            etCoinName.setText(coin.getName());
            etCoinSymbol.setText(coin.getSymbol());
        } else view.findViewById(R.id.bDelete).setVisibility(View.GONE);

        view.findViewById(R.id.bAccept).setOnClickListener(v -> {
            final String coinName = etCoinName.getText().toString();
            final String coinSymbol = etCoinSymbol.getText().toString();
            final Coin newCoin = new Coin(coinName, coinSymbol);
            if (!coinName.isEmpty() && !coinSymbol.isEmpty()) {
                if (!coins.exists(coinName) || coin != null && coin.getName().equals(coinName)) {
                    if (coin == null) resultCoinCreate(newCoin);
                    else resultCoinEdit(newCoin, coin);
                } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.coin_already_exists));
            } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.coin_no_name));
        });

        view.findViewById(R.id.bCancel).setOnClickListener(v -> closeFragment());
        view.findViewById(R.id.bDelete).setOnClickListener(v -> replaceFragment(CoinDeleter.newInstance(coin)));
    }
}