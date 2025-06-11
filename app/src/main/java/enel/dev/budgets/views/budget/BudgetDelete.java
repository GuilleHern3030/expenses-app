package enel.dev.budgets.views.budget;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.BudgetController;

public class BudgetDelete extends BudgetContext {

    private String budgetName;

    public BudgetDelete() {
        // Required empty public constructor
    }

    public static BudgetDelete newInstance(final String budgetName) {
        BudgetDelete fragment = new BudgetDelete();
        Bundle args = new Bundle();
        args.putString("name", budgetName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {
            this.budgetName = getArguments().getString("name");
        } catch(Exception ignored) { }
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

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_budget_delete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.bCancel).setOnClickListener(v -> closeFragment());
        view.findViewById(R.id.bAccept).setOnClickListener(v -> deleteBudget(budgetName));

        TextView budgetNameTextView = view.findViewById(R.id.budget_name);
        budgetNameTextView.setText(budgetName);
    }

    private void deleteBudget(final String budgetName) {
        BudgetController.delete(requireActivity(), budgetName);
        restartFragment();
    }
}