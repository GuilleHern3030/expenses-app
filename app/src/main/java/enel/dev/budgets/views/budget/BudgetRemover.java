package enel.dev.budgets.views.budget;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.BudgetController;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.budget.Budget;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BudgetRemover#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetRemover extends BudgetContext {

    private String budgetName;
    private Category category;
    public BudgetRemover() {
        // Required empty public constructor
    }

    public static BudgetRemover newInstance(final String budgetName, final Category category) {
        BudgetRemover fragment = new BudgetRemover();
        Bundle args = new Bundle();
        args.putString("name", budgetName);
        args.putString("categoryname", category.getName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Categories categories = Controller.categories(requireActivity()).get();
        if (getArguments() != null) try {
            this.budgetName = getArguments().getString("name");
            this.category = categories.getCategory(getArguments().getString("categoryname", ""));
        } catch(Exception ignored) { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_budget_remover, container, false);

        // Registra un callback para el botón "Atrás"
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        closeFragment();
                    }
                });

        view.findViewById(R.id.bCancel).setOnClickListener(v -> closeFragment());
        view.findViewById(R.id.bAccept).setOnClickListener(v -> removeBudget(category, budgetName));

        FrameLayout color = view.findViewById(R.id.category_color);
        ImageView icon = view.findViewById(R.id.category_icon);
        TextView name = view.findViewById(R.id.category_name);

        color.setBackgroundResource(category.getColor());
        icon.setImageResource(category.getImage());
        name.setText(category.getName());

        return view;
    }

    private void removeBudget(final Category category, final String budgetName) {
        boolean success = BudgetController.remove(requireActivity(), budgetName, category.getName());
        removeBudget(new Budget(category));
    }
}