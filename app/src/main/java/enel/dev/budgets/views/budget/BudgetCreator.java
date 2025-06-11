package enel.dev.budgets.views.budget;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.BudgetController;
import enel.dev.budgets.objects.budget.Budgets;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class BudgetCreator extends BudgetContext {

    private final static int MAX_CHARACTERS = 24;

    private EditText etBudgetName;
    private final ArrayList<String> budgetsStored = new ArrayList<>();

    public static BudgetCreator newInstance(final ArrayList<String> budgetsStored) {
        Bundle args = new Bundle();
        if (budgetsStored != null && budgetsStored.size() > 0)
            for (int i = 0; i < budgetsStored.size(); i++)
                args.putString("param" + i, budgetsStored.get(i));
        BudgetCreator fragment = new BudgetCreator();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            budgetsStored.addAll(Arrays.asList(params));
        } catch (Exception ignored) { }
    }

    public BudgetCreator() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Registra un callback para el botón "Atrás"
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        closeFragment();
                    }
                });

        return inflater.inflate(R.layout.fragment_budget_creator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etBudgetName = view.findViewById(R.id.etBudgetName);
        view.findViewById(R.id.bAccept).setOnClickListener(v -> createBudget());
        view.findViewById(R.id.bCancel).setOnClickListener(v -> closeFragment());

    }

    private void createBudget() {
        String budgetName = etBudgetName.getText().toString();
        if (budgetName.length() > 0) {
            if (budgetName.length() > MAX_CHARACTERS) budgetName = budgetName.substring(0, MAX_CHARACTERS);
            if (!budgetsStored.contains(budgetName)) {
                BudgetController.store(requireActivity(), new Budgets(budgetName));
                BudgetController.setFirst(requireActivity(), budgetName);
            }
            restartFragment();
        } else closeFragment();
    }
}