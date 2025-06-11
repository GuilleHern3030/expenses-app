package enel.dev.budgets.data.livedata;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.debt.Debts;

public class DebtsViewModel extends ViewModel {
    private final MutableLiveData<Debts> debts;
    private final MutableLiveData<Boolean> dataLoading;
    private final ExecutorService executorService;

    public DebtsViewModel() {
        debts = new MutableLiveData<>();
        dataLoading = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Debts> getDebts() {
        return debts;
    }

    public LiveData<Boolean> isDataLoading() {
        return dataLoading;
    }

    public void loadDebts(final Context context) {
        dataLoading.setValue(true);
        executorService.submit(() -> {
            Debts debtsList = Controller.debts(context).get();
            Log.d("DebtsViewModel", "Datos cargados: " + debtsList.size());
            debts.postValue(debtsList);
            dataLoading.postValue(false);
        });
    }
}