package enel.dev.budgets.views.configuration.menu;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Switch;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.money.Balance;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.utils.SnackBar;
import enel.dev.budgets.views.configuration.ConfigurationContext;
import enel.dev.budgets.views.editor.coin.CoinContext;
import enel.dev.budgets.views.editor.coin.CoinDeleter;
import enel.dev.budgets.views.editor.coin.CoinEditor;
import enel.dev.budgets.views.editor.coin.CoinListLayout;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class CoinConfiguration extends ConfigurationContext implements CoinContext.OnCoinInteractionListener, CoinListLayout.OnElementClickListener {

    EditText etCoinSymbolPreference;

    private FrameLayout fragmentAbove;
    private Balance balance;
    private CoinListLayout balanceList; // layout de la lista del balance de monedas

    public CoinConfiguration() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        balance = Controller.balances(requireActivity()).get();
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_configuration_coin, container, false);

        fragmentAbove = view.findViewById(R.id.coin_fragment_above);

        view.findViewById(R.id.bBack).setOnClickListener(v -> back());
        etCoinSymbolPreference = view.findViewById(R.id.coin_symbol);

        view.findViewById(R.id.save).setOnClickListener(v -> {
            String newCoinSymbol = etCoinSymbolPreference.getText().toString();
            if (newCoinSymbol.length() > 0) {
                Controller.setDefaultCoin(requireActivity(), newCoinSymbol);
                SnackBar.show(requireActivity(), requireView(), requireActivity().getString(R.string.coin_edition_success));
            }
        });

        Switch moreCoinsSwitch = view.findViewById(R.id.more_coins_switch);

        moreCoinsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            view.findViewById(R.id.more_coins_frame).setVisibility(isChecked ? View.VISIBLE : View.GONE);
            Preferences.setMoreCoinsAvailable(requireActivity(), isChecked);
        });

        if (Preferences.moreCoinsAvailable(requireActivity())) {
            moreCoinsSwitch.setChecked(true);
            Preferences.setDefaultCoin(requireActivity(), balance.get(0).getCoin());
        } else {
            moreCoinsSwitch.setChecked(false);
            view.findViewById(R.id.more_coins_frame).setVisibility(View.GONE);
        }

        balanceList = new CoinListLayout(requireContext(), view.findViewById(R.id.coins_list));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            Coin preferedCoin = balance.get(0).getCoin();
            etCoinSymbolPreference.setText(preferedCoin.getSymbol());
        } catch (Exception e) {
            Preferences.setDefaultCoin(requireActivity(), Preferences.DEFAULT_COIN_SYMBOL);
            etCoinSymbolPreference.setText(Preferences.DEFAULT_COIN_SYMBOL);
        }

        loadCoins();

        view.findViewById(R.id.add_new_coin).setOnClickListener(v -> showFragmentAbove(CoinEditor.newInstance()));

    }

    private void loadCoins() {
        balance = Controller.balances(requireActivity()).get();
        balanceList.showContent(balance);
        balanceList.setOnBalanceCLick(this);
    }

    private void showFragmentAbove(CoinContext fragment) {
        fragment.setOnCoinChangeListener(this);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.coin_fragment_above, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    private void hideFragmentAbove() {
        fragmentAbove.setVisibility(View.GONE);
        fragmentAbove.removeAllViews();
    }

    @Override
    public void onCancelOperation() {
        hideFragmentAbove();
    }

    @Override
    public void onCoinCreated(Coin coin) {
        hideFragmentAbove();
        loadCoins();
    }

    @Override
    public void onCoinDeleted(Coin coin) {
        hideFragmentAbove();
        if (coin.getName().equals(Preferences.defaultCoin(requireActivity()).getName()))
            Preferences.setDefaultCoin(requireActivity(), balance.get(0).getCoin());
        Controller.transactions(requireActivity()).deleteCoin(coin); // Elimina todas las transacciones de la moneda que se eliminó
        Controller.debts(requireActivity()).deleteCoin(coin); // Elimina todas las deudas de la moneda que se eliminó
        loadCoins();
    }

    @Override
    public void onCoinEdited(Coin newCoin, Coin oldCoin) {
        hideFragmentAbove();
        if (oldCoin.getName().equals(Preferences.defaultCoin(requireActivity()).getName()))
            Preferences.setDefaultCoin(requireActivity(), newCoin);
        Controller.transactions(requireActivity()).editCoin(oldCoin, newCoin);
        Controller.debts(requireActivity()).editCoin(oldCoin, newCoin);
        loadCoins();
    }

    @Override
    public void onReplaceFragmentRequired(CoinContext newFragment) {
        showFragmentAbove(newFragment);
    }

    @Override
    public void balanceClicked(int row, Money balance) {
        showFragmentAbove(CoinEditor.newInstance(balance.getCoin()));
    }

    @Override
    public boolean balanceLongClicked(int row, Money balance) {
        showFragmentAbove(CoinDeleter.newInstance(balance.getCoin()));
        return true;
    }
}