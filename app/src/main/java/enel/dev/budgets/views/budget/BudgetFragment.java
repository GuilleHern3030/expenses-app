package enel.dev.budgets.views.budget;

import android.annotation.SuppressLint;
import android.os.Bundle;

import enel.dev.budgets.data.livedata.BudgetsViewModel;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.budget.Budget;
import enel.dev.budgets.objects.budget.Budgets;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transactions;
import enel.dev.budgets.views.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import enel.dev.budgets.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BudgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetFragment extends Fragment implements BudgetContext.OnBudgetsChangeListener {

    private BudgetListLayout budgetListLayout;
    private TextView budgetTitleTextView;
    private TextView budgetTotalTextView;
    private ImageView abMenu;
    private ImageView bDelete;
    private ArrayList<String> budgetsList;
    private Budgets currentBudget;
    private Categories availableCategories;
    private Transactions transactions;
    private String coinSymbol = Preferences.DEFAULT_COIN_SYMBOL;

    public BudgetFragment() {
        // Required empty public constructor
    }

    public static BudgetFragment newInstance(String... params) {
        BudgetFragment fragment = new BudgetFragment();
        fragment.setArguments(bundle(params));
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            coinSymbol = Preferences.defaultCoin(requireActivity()).getSymbol(); //Controller.getBalance(requireActivity()).get(0).getCoin().getSymbol();
        } catch (Exception ignored) {
            coinSymbol = Preferences.DEFAULT_COIN_SYMBOL;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        fragmentAbove = view.findViewById(R.id.fragment_above);

        // Action bar
        this.abMenu = view.findViewById(R.id.menu);
        abMenu.setOnClickListener(v -> showFragmentAbove(BudgetMenuFragment.newInstance(budgetsList)));

        // Budgets
        budgetListLayout = new BudgetListLayout(
                requireActivity(),
                view.findViewById(R.id.budget_list),
                view.findViewById(R.id.budget_total_container),
                Preferences.decimalFormat(requireActivity()),
                coinSymbol
        );

        budgetTitleTextView = view.findViewById(R.id.budget_title);
        budgetTotalTextView = view.findViewById(R.id.budget_total);
        bDelete = view.findViewById(R.id.bDelete);

        view.findViewById(R.id.bDelete).setOnClickListener(v -> showFragmentAbove(BudgetDelete.newInstance(currentBudget.getName())));

        return view;
    }

    @Override
    public void onActionPressed() { // Add a Budget
        if (currentBudget != null)
            showFragmentAbove(BudgetAdder.newInstance(currentBudget, availableCategories));
        else showFragmentAbove(BudgetCreator.newInstance(budgetsList));
    }

    @Override
    public void onBackPressed() {
        goHome();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadBudgets();
        view.findViewById(R.id.navigation_view_budgets).setOnClickListener(v -> {});
    }

    private void loadBudgets() {
        loadBudgets(0);
    }
    private void loadBudgets(final String budgetsName) {
        loadBudgets(-1, budgetsName);
    }
    private void loadBudgets(final int id) {
        loadBudgets(id, null);
    }
    private void loadBudgets(final int id, final String budgetsName) {

        BudgetsViewModel viewModel = new ViewModelProvider(this).get(BudgetsViewModel.class);

        // Observa el estado de carga
        viewModel.isDataLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                budgetListLayout.showLoading();
                budgetTitleTextView.setText(requireActivity().getString(R.string.budget));
                bDelete.setVisibility(View.GONE);
                abMenu.setOnClickListener(v -> { });
            } else abMenu.setOnClickListener(v -> showFragmentAbove(BudgetMenuFragment.newInstance(budgetsList)));
        });

        // Observa los datos cargados
        viewModel.getBudgetsList().observe(getViewLifecycleOwner(), budgetsArray -> budgetsList = budgetsArray);
        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactionsArray -> transactions = transactionsArray);
        viewModel.getBudgets().observe(getViewLifecycleOwner(), budgets -> {
            currentBudget = budgets;
            showBudget(budgets);
            actualizeCategories(budgets);
        });

        // Cargar los datos
        if (budgetsName != null && budgetsName.length() > 0)
            viewModel.loadBudgets(requireActivity(), budgetsName);
        else
            viewModel.loadBudgets(requireActivity(), id);
    }

    @SuppressLint("SetTextI18n")
    private void showBudget(final Budgets budget) {

        if (budget != null) {
            budgetTitleTextView.setText(budget.getName());
            bDelete.setVisibility(View.VISIBLE);

            final double total = budget.total();
            if (total > 0) {
                budgetTotalTextView.setText(
                        Money.toString(coinSymbol, total, Preferences.decimalFormat(requireActivity()))
                );
            }
        }

        budgetListLayout.showContent(budget, transactions);
        budgetListLayout.setOnBudgetCLickListener(new BudgetListLayout.OnElementClickListener() {
            @Override
            public void budgetClicked(int id, Budget budget) {
                showFragmentAbove(BudgetEditor.newInstance(currentBudget.getName(), budget.getCategory(), budget.getAmount()));
            }

            @Override
            public boolean budgetLongClicked(int id, Budget budget) {
                showFragmentAbove(BudgetRemover.newInstance(currentBudget.getName(), budget.getCategory()));
                return true;
            }
        });
    }

    private void actualizeCategories(final Budgets budgets) {
        availableCategories = Controller.categories(requireActivity()).get().expenses();
        if (budgets != null && budgets.size() > 0) for (Budget budget : budgets)
            availableCategories.remove(budget.getCategory().getName());
    }

    private void showFragmentAbove(BudgetContext fragment) {
        fragment.setOnBudgetsChangeListener(this);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBudgetRemoved(Budget budget) {
        try {
            hideFragmentAbove();
            final int index = this.currentBudget.indexOf(budget.getCategory().getName());
            this.currentBudget.remove(index);
            actualizeCategories(this.currentBudget);
            showBudget(this.currentBudget);
        } catch(Exception ignored) {
            loadBudgets(currentBudget.getName());
        }
    }

    @Override
    public void onBudgetReloadRequired() {
        hideFragmentAbove();
        loadBudgets();
    }

    @Override
    public void onBudgetAdded(Budget budget) {
        try {
            hideFragmentAbove();
            this.currentBudget.add(budget);
            actualizeCategories(this.currentBudget);
            showBudget(this.currentBudget);
        } catch(Exception ignored) {
            loadBudgets(currentBudget.getName());
        }
    }

    @Override
    public void onBudgetEdited(Budget budget) {
        try {
            hideFragmentAbove();
            final int index = this.currentBudget.indexOf(budget.getCategory().getName());
            this.currentBudget.set(index, budget);
            actualizeCategories(this.currentBudget);
            showBudget(this.currentBudget);
        } catch(Exception ignored) {
            loadBudgets(currentBudget.getName());
        }
    }

    @Override
    public void onFragmentChanged(BudgetContext frame) {
        showFragmentAbove(frame);
    }

    @Override
    public void onCancel() {
        hideFragmentAbove();
    }
}