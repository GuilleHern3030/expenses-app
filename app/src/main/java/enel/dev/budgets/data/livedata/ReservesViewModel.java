package enel.dev.budgets.data.livedata;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.reserve.Reserves;

public class ReservesViewModel extends ViewModel {
    private final MutableLiveData<Reserves> reserves;
    private final MutableLiveData<Boolean> dataLoading;
    private final ExecutorService executorService;

    public ReservesViewModel() {
        reserves = new MutableLiveData<>();
        dataLoading = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Reserves> getReserves() {
        return reserves;
    }

    public LiveData<Boolean> isDataLoading() {
        return dataLoading;
    }

    public void loadReserves(final Context context) {
        dataLoading.setValue(true);
        executorService.submit(() -> {
            Reserves reservesList = Controller.reserves(context).get();
            Log.d("ReservesViewModel", "Datos cargados: " + reservesList.size());
            reserves.postValue(reservesList);
            dataLoading.postValue(false);
        });
    }
}