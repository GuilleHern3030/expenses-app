package enel.dev.budgets.data.livedata;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import enel.dev.budgets.data.sql.BudgetController;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.budget.Budgets;
import enel.dev.budgets.objects.transaction.Transactions;

public class BudgetsViewModel extends ViewModel {
    private final MutableLiveData<Budgets> budgets;
    private final MutableLiveData<Transactions> transactions;
    private final MutableLiveData<Boolean> dataLoading;
    private final MutableLiveData<ArrayList<String>> budgetsList;
    private final ExecutorService executorService;


    public BudgetsViewModel() {
        budgets = new MutableLiveData<>();
        transactions = new MutableLiveData<>();
        dataLoading = new MutableLiveData<>();
        budgetsList = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Budgets> getBudgets() {
        return budgets;
    }

    public LiveData<Transactions> getTransactions() {
        return transactions;
    }

    public LiveData<ArrayList<String>> getBudgetsList() {
        return budgetsList;
    }

    public LiveData<Boolean> isDataLoading() {
        return dataLoading;
    }

    public void loadBudgets(final Context context, final int id) {
        dataLoading.setValue(true);
        executorService.submit(() -> {

            Budgets budgetsLoaded = BudgetController.get(context, id);
            Transactions transactionsLoaded = Controller.transactions(context).get().expenses().getCategoriesSorted();
            ArrayList<String> budgetsListLoaded = BudgetController.budgets(context);

            budgetsList.postValue(budgetsListLoaded);
            transactions.postValue(transactionsLoaded);
            budgets.postValue(budgetsLoaded);

            dataLoading.postValue(false);
        });
    }

    public void loadBudgets(final Context context, final String budgetName) {
        dataLoading.setValue(true);
        executorService.submit(() -> {

            Budgets budgetsLoaded = BudgetController.get(context, budgetName);
            Transactions transactionsLoaded = Controller.transactions(context).get().expenses().getCategoriesSorted();
            ArrayList<String> budgetsListLoaded = BudgetController.budgets(context);

            budgetsList.postValue(budgetsListLoaded);
            transactions.postValue(transactionsLoaded);
            budgets.postValue(budgetsLoaded);

            dataLoading.postValue(false);
        });
    }
}