package enel.dev.budgets.views.editor.debt;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.debt.Debt;
import enel.dev.budgets.objects.debt.Debts;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.utils.SnackBar;
import enel.dev.budgets.views.editor.debt.CalculatorLayout;
import enel.dev.budgets.views.editor.transaction.TransactionFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DebtFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DebtFragment extends Fragment {

    private FrameLayout fragmentAbove;

    // Transaction attributes
    protected int id;
    protected Money money;
    protected String description;
    protected String lenderName;
    private int index;
    private boolean deleteOption = false;

    private EditText etLenderName;
    private EditText etDescription;

    private Coin coin;

    public DebtFragment() {
        // Required empty public constructor
    }

    // Create a new debt
    public static DebtFragment newInstance() {
        DebtFragment fragment = new DebtFragment();
        Bundle args = new Bundle();
        args.putInt("id", -1);
        fragment.setArguments(args);
        return fragment;
    }

    // Edit a exists debt
    public static DebtFragment newInstance(final Debt debt, final int index) {
        return newInstance(debt, index, false);
    }

    // Delete a debt
    public static DebtFragment newInstance(final Debt debt, final int index, final boolean delete) {
        DebtFragment fragment = new DebtFragment();
        Bundle args = new Bundle();
        args.putInt("id", debt.id());
        args.putInt("index", index);
        args.putString("coinname", debt.getMoney().getCoin().getName());
        args.putDouble("coinamount", debt.getMoney().getAmount());
        args.putString("description", debt.getDescription());
        args.putString("lender", debt.getLender());
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
            final String coinName = getArguments().getString("coinname", Preferences.defaultCoin(requireActivity()).getName());
            final double coinAmount = getArguments().getDouble("coinamount", 0);
            this.description = getArguments().getString("description", "");
            this.lenderName = getArguments().getString("lender", "");

            Log.i("DebtFragment", "id = " + this.id);
            Log.i("DebtFragment", "coinname = " + coinName);
            Log.i("DebtFragment", "coinamount = " + coinAmount);
            Log.i("DebtFragment", "description = " + this.description);
            Log.i("DebtFragment", "lendername = " + this.lenderName);

            this.coin = coinName != null ? Controller.balances(requireActivity()).get().getCoin(coinName) : Preferences.defaultCoin(requireActivity());

            this.money = new Money(coin, coinAmount);

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

        return inflater.inflate(R.layout.fragment_debt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentAbove = view.findViewById(R.id.secondary_debt_fragment);
        etDescription = view.findViewById(R.id.description_text);
        etLenderName = view.findViewById(R.id.lender_name);

        if (description != null && !description.isEmpty())
            etDescription.setText(description);

        if (lenderName != null && !lenderName.isEmpty())
            etLenderName.setText(lenderName);

        TextView inputText = view.findViewById(R.id.debt_amount);
        inputText.setText(money.toString(Preferences.decimalFormat(requireActivity())));

        view.findViewById(R.id.calculator_container).setOnClickListener(v -> showAmountInput(inputText));
        view.findViewById(R.id.bCancelOperation).setOnClickListener(v -> cancelOperation());
        view.findViewById(R.id.bDelete).setOnClickListener(v -> deleteDebt());
        view.findViewById(R.id.bChangeDebt).setOnClickListener(v -> {
            if(id >= 0)
                editDebt();
            else
                createDebt();
        });

        if (this.id >= 0)
            view.findViewById(R.id.bDelete).setVisibility(View.VISIBLE);
        else
            showAmountInput(inputText);

        if (deleteOption)
            deleteDebt();

    }

    private void editDebt() {

        if(money.getAmount() > 0 && !etLenderName.getText().toString().isEmpty()) {
            Controller.debts(requireActivity()).edit(new Debt(
                    id,
                    etLenderName.getText().toString(),
                    money,
                    etDescription.getText().toString()));
            successOperation();
        } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.debt_requires));
    }

    private void createDebt() {
        if(money.getAmount() > 0 && !etLenderName.getText().toString().isEmpty()) {
            final int newId = Controller.debts(requireActivity()).get().getUnusedId();
            Controller.debts(requireActivity()).add(new Debt(
                    newId,
                    etLenderName.getText().toString(),
                    money,
                    etDescription.getText().toString()
            ));
            successOperation();
        } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.debt_requires));
    }

    private void deleteDebt() {
        DeleteLayout fragment = DeleteLayout.newInstance(new Debt(
                id,
                etLenderName.getText().toString(),
                money,
                etDescription.getText().toString()));
        fragment.setOnDeleteListener(new DeleteLayout.OnDeleteListener() {
            @Override
            public void onSuccessDelete() {
                Controller.debts(requireActivity()).delete(id);
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
    private OnDebtChangeListener listener;
    public interface OnDebtChangeListener {
        void onDebtChanged();
        void onCancelDebtOperation();
    }

    public void setOnDebtChangeListener(OnDebtChangeListener listener) {
        this.listener = listener;
    }

    private void cancelOperation() {
        listener.onCancelDebtOperation();
    }

    private void successOperation() {
        listener.onDebtChanged();
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
                CalculatorLayout.newInstance(false, money.getAmount(), coin.getSymbol()):
                CalculatorLayout.newInstance(false, 0, coin.getSymbol());
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