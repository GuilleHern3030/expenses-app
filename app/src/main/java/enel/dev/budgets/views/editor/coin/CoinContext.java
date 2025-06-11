package enel.dev.budgets.views.editor.coin;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.utils.SnackBar;

public class CoinContext extends Fragment {




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Registra un callback para el botón "Atrás"
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        goBack();
                    }
                });
    }

    protected void goBack() {
        if (subListener != null) subListener.onCancelOperation();
        getParentFragmentManager().beginTransaction().remove(this).commit(); // Remove this fragment
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.subListener = null;
    }

    public void setOnCoinChangeListener(OnCoinInteractionListener listener) {
        this.subListener = listener;
    }

    private OnCoinInteractionListener subListener;
    public interface OnCoinInteractionListener {

        void onCancelOperation();
        void onCoinCreated(final Coin coin);
        void onCoinDeleted(final Coin coin);
        void onCoinEdited(final Coin coin, final Coin oldCoin);
        void onReplaceFragmentRequired(CoinContext newFragment);
    }

    protected void closeFragment() {
        goBack();
    }

    protected void replaceFragment(CoinContext fragment) {
        if (subListener != null) subListener.onReplaceFragmentRequired(fragment);
        getParentFragmentManager().beginTransaction().remove(this).commit(); // Remove this fragment
    }

    protected void resultCoinCreate(final Coin coin) {
        boolean success = Controller.balances(requireActivity()).add(coin);
        if (success) {
            if (subListener != null) subListener.onCoinCreated(coin);
            getParentFragmentManager().beginTransaction().remove(this).commit(); // Remove this fragment
        } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.coin_operation_failed));
    }

    protected void resultCoinDelete(final Coin coin) {
        boolean success = Controller.balances(requireActivity()).delete(coin);
        if (success) {
            if (subListener != null) subListener.onCoinDeleted(coin);
            getParentFragmentManager().beginTransaction().remove(this).commit(); // Remove this fragment
        } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.coin_operation_failed));
    }

    protected void resultCoinEdit(final Coin coin, final Coin oldCoin) {
        boolean success = Controller.balances(requireActivity()).edit(oldCoin.getName(), coin);
        if (success) {
            if (subListener != null) subListener.onCoinEdited(coin, oldCoin);
            getParentFragmentManager().beginTransaction().remove(this).commit(); // Remove this fragment
        } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.coin_operation_failed));
    }

}
