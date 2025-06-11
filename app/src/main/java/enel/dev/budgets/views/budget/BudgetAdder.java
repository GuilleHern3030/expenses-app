package enel.dev.budgets.views.budget;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.BudgetController;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.budget.Budget;
import enel.dev.budgets.objects.budget.Budgets;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.utils.SnackBar;
import enel.dev.budgets.views.editor.category.CategoriesSpinner;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BudgetAdder#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetAdder extends BudgetContext {

    private String budgetName;
    private final Categories availableCategories = new Categories();
    private Spinner spinnerCategories;

    public BudgetAdder() {
        // Required empty public constructor
    }

    public static BudgetAdder newInstance(final Budgets budgets, final Categories availableCategories) {
        BudgetAdder fragment = new BudgetAdder();
        Bundle args = new Bundle();
        args.putString("name", budgets.getName());
        for (int i = 0; i < availableCategories.size(); i++) {
            args.putString("param"+i, availableCategories.get(i).getName());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Categories categories = Controller.categories(requireActivity()).get();
        if (getArguments() != null) try {
            for (String param : params) availableCategories.add(categories.getCategory(param));
            this.budgetName = getArguments().getString("name");

        } catch(Exception ignored) { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        closeFragment();
                    }
                });

        return inflater.inflate(R.layout.fragment_budget_adder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvBudgetName = view.findViewById(R.id.budget_name);
        tvBudgetName.setText(budgetName);

        spinnerCategories = view.findViewById(R.id.spinner_categories);
        CategoriesSpinner adapter = new CategoriesSpinner(requireActivity(), availableCategories);
        spinnerCategories.setAdapter(adapter);

        view.findViewById(R.id.bAccept).setOnClickListener(v -> addCategory(spinnerCategories));
        view.findViewById(R.id.bCancel).setOnClickListener(v -> closeFragment());

        if (availableCategories.size() == 0) {
            SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.no_available_categories));
            closeFragment();
        }
    }

    private void addCategory(final Spinner spinner) {
        final int id = spinner.getSelectedItemPosition();
        final Budget newBudget = new Budget(availableCategories.get(id));
        BudgetController.add(requireActivity(), budgetName, newBudget);
        addBudget(newBudget);
    }
}