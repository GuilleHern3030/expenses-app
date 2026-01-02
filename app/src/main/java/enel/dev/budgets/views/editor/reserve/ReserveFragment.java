package enel.dev.budgets.views.editor.reserve;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.reserve.Reserve;
import enel.dev.budgets.utils.SnackBar;

public class ReserveFragment extends Fragment {

    private FrameLayout fragmentAbove;

    // Transaction attributes
    protected int id;
    protected Money money;
    protected String description;
    protected String lenderName;
    protected String name;
    private int index;
    private boolean deleteOption = false;

    private EditText etLenderName;

    private Coin coin;
    private boolean fetching = false;

    public ReserveFragment() {
        // Required empty public constructor
    }

    // Create a new debt
    public static ReserveFragment newInstance() {
        ReserveFragment fragment = new ReserveFragment();
        Bundle args = new Bundle();
        args.putInt("id", -1);
        fragment.setArguments(args);
        return fragment;
    }

    // Edit a exists debt
    public static ReserveFragment newInstance(final Reserve debt, final int index) {
        return newInstance(debt, index, false);
    }

    // Delete a debt
    public static ReserveFragment newInstance(final Reserve debt, final int index, final boolean delete) {
        ReserveFragment fragment = new ReserveFragment();
        Bundle args = new Bundle();
        args.putInt("id", debt.id());
        args.putInt("index", index);
        args.putString("name", debt.getName());
        args.putDouble("amount", debt.getAmount());
        args.putBoolean("delete", delete);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.id = getArguments().getInt("id", -1);
            this.index = getArguments().getInt("index", -1);
            this.name = getArguments().getString("name", Preferences.defaultCoin(requireActivity()).getName());
            final double amount = getArguments().getDouble("amount", 0);

            this.coin = Preferences.defaultCoin(requireActivity());

            this.money = new Money(coin, amount);

            this.deleteOption = getArguments().getBoolean("delete", false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        cancelOperation();
                    }
                });

        return inflater.inflate(R.layout.fragment_reserve, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentAbove = view.findViewById(R.id.secondary_debt_fragment);
        etLenderName = view.findViewById(R.id.lender_name);

        if (lenderName != null && !lenderName.isEmpty())
            etLenderName.setText(lenderName);

        TextView inputText = view.findViewById(R.id.debt_amount);
        inputText.setText(money.toString(Preferences.decimalFormat(requireActivity())));

        view.findViewById(R.id.calculator_container).setOnClickListener(v -> showAmountInput(inputText));
        view.findViewById(R.id.bCancelOperation).setOnClickListener(v -> cancelOperation());
        view.findViewById(R.id.bDelete).setOnClickListener(v -> deleteDebt());
        view.findViewById(R.id.bChangeDebt).setOnClickListener(v -> {
            if (!fetching) {
                fetching = true;
                if (id >= 0)
                    editDebt();
                else
                    createDebt();
            }
        });

        if (this.id >= 0)
            view.findViewById(R.id.bDelete).setVisibility(View.VISIBLE);
        else
            showAmountInput(inputText);

        if (deleteOption)
            deleteDebt();

    }

    private void editDebt() {

        if(money.getAmount() > 0) {
            Controller.reserves(requireActivity()).edit(new Reserve(
                    id,
                    etLenderName.getText().toString(),
                    money.getAmount()),
                    new Controller.SQLcallback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        fetching = false;
                        successOperation();
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
        } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.reserve_requires));
    }

    private void createDebt() {
        if(money.getAmount() > 0) {
            final int newId = Controller.debts(requireActivity()).get().getUnusedId();
            Controller.reserves(requireActivity()).add(new Reserve(
                    newId,
                    etLenderName.getText().toString(),
                    money.getAmount()), new Controller.SQLcallback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        fetching = false;
                        successOperation();
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
        } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.reserve_requires));
    }

    private void deleteDebt() {
        DeleteLayout fragment = DeleteLayout.newInstance(new Reserve(
                id,
                etLenderName.getText().toString(),
                money.getAmount()));
        fragment.setOnDeleteListener(new DeleteLayout.OnDeleteListener() {
            @Override
            public void onSuccessDelete() {
                hideFragmentAbove();
                successOperation();
            }

            @Override
            public void onCancelDelete() {
                hideFragmentAbove();
                if (deleteOption)
                    cancelOperation();
            }
        });
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.secondary_debt_fragment, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    private OnReserveChangeListener listener;
    public interface OnReserveChangeListener {
        void onReserveChanged();
        void onCancelReserveOperation();
    }

    public void setOnReserveChangeListener(OnReserveChangeListener listener) {
        this.listener = listener;
    }

    private void cancelOperation() {
        listener.onCancelReserveOperation();
    }

    private void successOperation() {
        listener.onReserveChanged();
    }
    //</editor-fold>

    private void hideFragmentAbove() {
        fragmentAbove.setVisibility(View.GONE);
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        cancelOperation();
                    }
                });
    }

    private void showAmountInput(final TextView inputText) {
        CalculatorLayout fragment = money != null ?
                CalculatorLayout.newInstance(true, money.getAmount(), coin.getSymbol()):
                CalculatorLayout.newInstance(true, 0, coin.getSymbol());
        fragment.setOnCalculatorListener(new CalculatorLayout.OnCalculatorListener() {
            @Override
            public void onAccept(double amount) {
                hideFragmentAbove();
                money = new Money(coin, amount);
                inputText.setText(money.toString(Preferences.decimalFormat(requireActivity())));
            }

            @Override
            public void onCancel() {
                if (money == null || money.getAmount() == 0)
                    cancelOperation();
                hideFragmentAbove();
            }
        });
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.secondary_debt_fragment, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }
}