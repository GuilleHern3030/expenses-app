package enel.dev.budgets.views.editor.transaction;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.utils.SnackBar;
import enel.dev.budgets.views.editor.Calculator;

public class CalculatorLayout extends Fragment {

    public CalculatorLayout() {
        // Required empty public constructor
    }

    private double initInput;
    private boolean isAnIncome;
    private String coinSymbol = Preferences.DEFAULT_COIN_SYMBOL;
    private Calculator calculator;

    public static CalculatorLayout newInstance() {
        return newInstance(false, 0, Preferences.DEFAULT_COIN_SYMBOL);
    }

    public static CalculatorLayout newInstance(final boolean isAnIncome) {
        return newInstance(isAnIncome, 0, Preferences.DEFAULT_COIN_SYMBOL);
    }

    public static CalculatorLayout newInstance(final boolean isAnIncome, String coinSymbol) {
        return newInstance(isAnIncome, 0, coinSymbol);
    }

    public static CalculatorLayout newInstance(final boolean isAnIncome,  double amount) {
        return newInstance(isAnIncome, amount, Preferences.DEFAULT_COIN_SYMBOL);
    }

    public static CalculatorLayout newInstance(final boolean isAnIncome, final double amount, final String coinSymbol) {
        CalculatorLayout fragment = new CalculatorLayout();
        Bundle args = new Bundle();
        args.putDouble("amount", amount);
        args.putString("symbol", coinSymbol);
        args.putBoolean("isanincome", isAnIncome);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initInput = getArguments().getDouble("amount", 0);
            coinSymbol = getArguments().getString("symbol", Preferences.DEFAULT_COIN_SYMBOL);
            isAnIncome = getArguments().getBoolean("isanincome", false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        listener.onCancel();
                    }
                });
        return inflater.inflate(R.layout.fragment_transaction_input, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        @ColorInt int incomeTextColor = ContextCompat.getColor(requireActivity(), R.color.income_text);
        @ColorInt int expenseTextColor = ContextCompat.getColor(requireActivity(), R.color.expense_text);

        calculator = new Calculator(
                requireActivity(),
                view.findViewById(R.id.calculator),
                Preferences.decimalFormat(requireActivity()),
                coinSymbol
        );

        calculator.showCalculator(
                initInput,
                isAnIncome ? incomeTextColor : expenseTextColor,
                isAnIncome ? R.drawable.border_input_income : R.drawable.border_input_expense
        );

        view.findViewById(R.id.bCancel).setOnClickListener(v -> listener.onCancel());
        view.findViewById(R.id.bAccept).setOnClickListener(v -> {
            final double amount = calculator.getInput();
            if (amount > 0) {
                listener.onAccept(amount);
            } else SnackBar.show(requireActivity(), view, requireActivity().getString(R.string.error_invalid_amount));
        });
    }

    // Listener
    private OnCalculatorListener listener;
    public interface OnCalculatorListener {
        void onAccept(double amount);
        void onCancel();
    }

    public void setOnCalculatorListener(OnCalculatorListener listener) {
        this.listener = listener;
    }
}