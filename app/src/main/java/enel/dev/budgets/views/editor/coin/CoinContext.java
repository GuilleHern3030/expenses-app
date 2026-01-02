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

    private boolean fetching = false;


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
        if (!fetching) {
            fetching = true;
            final Fragment fragment = this;
            Controller.balances(requireActivity()).add(coin, new Controller.SQLcallback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        fetching = false;
                        if (subListener != null) subListener.onCoinCreated(coin);
                        getParentFragmentManager().beginTransaction().remove(fragment).commit(); // Remove this fragment
                    }
                }

                @Override
                public void onError(String error) {
                    if (isAdded()) {
                        fetching = false;
                        SnackBar.show(requireActivity(), getView(), error);
                    }
                }

                @Override
                public void onNetworkError() {
                    if (isAdded()) {
                        fetching = false;
                        SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.network_error));
                    }
                }
            });
        }
    }

    protected void resultCoinDelete(final Coin coin) {
        final Fragment fragment = this;
        Controller.balances(requireActivity()).delete(coin, new Controller.SQLcallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    if (subListener != null) subListener.onCoinDeleted(coin);
                    getParentFragmentManager().beginTransaction().remove(fragment).commit(); // Remove this fragment
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded())
                    SnackBar.show(requireActivity(), getView(), error);
            }

            @Override
            public void onNetworkError() {
                if (isAdded())
                    SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.network_error));
            }
        });
    }

    protected void resultCoinEdit(final Coin coin, final Coin oldCoin) {
        if (!fetching) {
            fetching = true;
            final Fragment fragment = this;
            Controller.balances(requireActivity()).edit(oldCoin.getName(), coin, new Controller.SQLcallback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        fetching = false;
                        if (subListener != null) subListener.onCoinEdited(coin, oldCoin);
                        getParentFragmentManager().beginTransaction().remove(fragment).commit(); // Remove this fragment
                    }
                }

                @Override
                public void onError(String error) {
                    if (isAdded())
                        SnackBar.show(requireActivity(), getView(), error);
                }

                @Override
                public void onNetworkError() {
                    if (isAdded()) {
                        fetching = false;
                        SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.network_error));
                    }
                }
            });
        }
    }

}
