package enel.dev.budgets.views.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.budget.Budget;
import enel.dev.budgets.objects.budget.Budgets;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;

public abstract class BudgetContext extends Fragment {

    protected String[] params;
    protected String budgetName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {
            this.budgetName = getArguments().getString("name");
            ArrayList<String> args = new ArrayList<>();
            int key = 0;
            while (getArguments().getString("param"+key) != null) {
                args.add(getArguments().getString("param"+key));
                key ++;
            }
            params = args.toArray(new String[0]);
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

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    private OnBudgetsChangeListener listener;
    public interface OnBudgetsChangeListener {
        void onBudgetRemoved(final Budget budget);
        void onBudgetReloadRequired();
        void onBudgetAdded(final Budget budget);
        void onBudgetEdited(final Budget budget);
        void onFragmentChanged(final BudgetContext frame);
        void onCancel();
    }

    public void setOnBudgetsChangeListener(OnBudgetsChangeListener listener) {
        this.listener = listener;
    }
    //</editor-fold>

    protected void closeFragment() {
        listener.onCancel();
    }

    protected void addBudget(final Budget budget) {
        listener.onBudgetAdded(budget);
    }

    protected void editBudget(final Budget budget) {
        listener.onBudgetEdited(budget);
    }

    protected void removeBudget(final Budget budget) {
        listener.onBudgetRemoved(budget);
    }

    protected void replaceFragment(final BudgetContext fragment) {
        listener.onFragmentChanged(fragment);
    }

    protected void restartFragment() {
        listener.onBudgetReloadRequired();
    }


}
