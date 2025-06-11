package enel.dev.budgets.data.livedata;
import android.app.Activity;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.money.Balance;

public class BalanceViewModel extends ViewModel {
    private final MutableLiveData<Balance> balance;
    private final MutableLiveData<Boolean> dataLoading;
    private final ExecutorService executorService;

    public BalanceViewModel() {
        balance = new MutableLiveData<>();
        dataLoading = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Balance> getBalance() {
        return balance;
    }

    public LiveData<Boolean> isDataLoading() {
        return dataLoading;
    }

    public void loadBalance(final Activity context) {
        dataLoading.setValue(true);
        executorService.submit(() -> {
            Balance balanceList = Controller.balances(context).get();
            Log.d("BalanceViewModel", "Datos cargados: " + balanceList.size());
            balance.postValue(balanceList);
            dataLoading.postValue(false);
        });
    }
}