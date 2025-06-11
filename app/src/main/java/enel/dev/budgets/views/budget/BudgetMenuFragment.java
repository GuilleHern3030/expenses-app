package enel.dev.budgets.views.budget;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.BudgetController;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BudgetMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetMenuFragment extends BudgetContext {

    private final ArrayList<String> budgetsList = new ArrayList<>();
    private BudgetMenuListLayout budgetListLayout;

    public BudgetMenuFragment() {
        // Required empty public constructor
    }

    public static BudgetMenuFragment newInstance(final ArrayList<String> budgetsList) {
        BudgetMenuFragment fragment = new BudgetMenuFragment();
        Bundle args = new Bundle();
        if (budgetsList != null && budgetsList.size() > 0)
            for (int i = 0; i < budgetsList.size(); i++)
                args.putString("param" + i, budgetsList.get(i));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            budgetsList.addAll(Arrays.asList(params));
        } catch (Exception ignored) { }
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

        return inflater.inflate(R.layout.fragment_budget_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.background).setOnClickListener(v -> closeFragment());
        view.findViewById(R.id.scrollview).setOnClickListener(v -> closeFragment());
        view.findViewById(R.id.bAddBudget).setOnClickListener(v -> replaceFragment(BudgetCreator.newInstance(budgetsList)));
        budgetListLayout = new BudgetMenuListLayout(requireContext(), view.findViewById(R.id.budget_list));
        budgetListLayout.showContent(budgetsList);
        budgetListLayout.setOnBudgetCLickListener(new BudgetMenuListLayout.OnElementClickListener() {
            @Override
            public void budgetClicked(String budgetsName) {
                BudgetController.setFirst(requireActivity(), budgetsName);
                restartFragment();
            }

            @Override
            public boolean budgetLongClicked(String budgetsName) {
                replaceFragment(BudgetDelete.newInstance(budgetsName));
                return true;
            }
        });
    }
}