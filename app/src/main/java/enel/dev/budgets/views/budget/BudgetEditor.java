package enel.dev.budgets.views.budget;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.data.sql.BudgetController;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.budget.Budget;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.views.editor.Calculator;

/**
 * A simple {@link Fragment} subclass.
 */
public class BudgetEditor extends BudgetContext {

    private String budgetName;
    private Category category;
    private double initAmount;
    private Calculator calculator;

    public static BudgetEditor newInstance(final String budgetName, final Category category, final double amount) {
        Bundle args = new Bundle();
        args.putDouble("amount", amount);
        args.putString("name", budgetName);
        args.putString("categoryname", category.getName());
        BudgetEditor fragment = new BudgetEditor();
        fragment.setArguments(args);
        return fragment;
    }

    public static BudgetEditor newInstance(final String budgetName, final Category category) {
        return newInstance(budgetName, category, 0);
    }

    public BudgetEditor() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Categories categories = Controller.categories(requireActivity()).get();
        if (getArguments() != null) try {
            this.budgetName = getArguments().getString("name");
            this.category = categories.getCategory(getArguments().getString("categoryname", ""));
            initAmount = getArguments().getDouble("amount", 0);
        } catch(Exception ignored) { }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget_editor, container, false);

        // Registra un callback para el botón "Atrás"
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        closeFragment();
                    }
                });

        // Calculator
        calculator = new Calculator(
                requireActivity(),
                view.findViewById(R.id.calculator),
                Preferences.decimalFormat(requireActivity()),
                Preferences.defaultCoin(requireActivity()).getSymbol()
        );

        calculator.showCalculator(initAmount);

        // Budget title name
        TextView budgetName = view.findViewById(R.id.budget_name);
        budgetName.setText(this.budgetName);

        // Buttons
        view.findViewById(R.id.bCancel).setOnClickListener(v -> closeFragment());
        view.findViewById(R.id.bDelete).setOnClickListener(v -> removeBudget());
        view.findViewById(R.id.bAccept).setOnClickListener(v -> editBudget(calculator.getInput()));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FrameLayout categoryFrame = view.findViewById(R.id.category_container);
        View categoryView = LayoutInflater.from(requireContext()).inflate(R.layout.spinner_category, categoryFrame, false);

        FrameLayout categoryColor = categoryView.findViewById(R.id.icon_container);
        ImageView categoryIcon = categoryView.findViewById(R.id.icon);
        TextView categoryName = categoryView.findViewById(R.id.text);

        categoryColor.setBackgroundResource(category.getColor());
        categoryIcon.setImageResource(category.getImage());
        categoryName.setText(category.getName());

        categoryFrame.addView(categoryView);
    }

    private void removeBudget() {
        replaceFragment(BudgetRemover.newInstance(budgetName, category));
    }

    private void editBudget(final double amount) {
        boolean success = BudgetController.edit(requireActivity(), budgetName, category.getName(), amount);
        editBudget(new Budget(category, amount));
    }
}