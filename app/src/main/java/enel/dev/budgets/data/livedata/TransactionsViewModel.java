package enel.dev.budgets.data.livedata;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.data.sql.TransactionsSQL;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.transaction.Transactions;

@SuppressWarnings("unused")
public class TransactionsViewModel extends ViewModel {
    private MutableLiveData<Transactions> transactions;
    private MutableLiveData<Boolean> dataLoading;

    private Date initDate;
    private Date endDate;

    public TransactionsViewModel() {
        transactions = new MutableLiveData<>();
        dataLoading = new MutableLiveData<>();
    }

    public LiveData<Transactions> getTransactions() {
        return transactions;
    }

    public LiveData<Boolean> isDataLoading() {
        return dataLoading;
    }

    public void loadTransactions(final Context context, final Date date, final Date date2) {
        //final ExecutorService executorService = Executors.newSingleThreadExecutor();
        initDate = date;
        endDate = date2;
        dataLoading.setValue(true);
        Controller.transactions(context).get(date, date2, new TransactionsSQL.TransactionsCallback() {
            @Override
            public void onSuccess(Transactions transactionsList) {
                transactions.postValue(transactionsList);
                dataLoading.postValue(false);
            }

            @Override
            public void onFailure(int errorCode) {
                transactions.postValue(new Transactions());
                dataLoading.postValue(false);
            }
        });
        /*executorService.submit(() -> {
            Transactions transactionsList = Controller.transactions(context).get(date, date2);
            transactions.postValue(transactionsList);
            dataLoading.postValue(false);
        });*/
    }

    public void loadTransactions(final Context context) {
        initDate = new Date();
        endDate = initDate;
        //final ExecutorService executorService = Executors.newSingleThreadExecutor();
        dataLoading.setValue(true);
        Controller.transactions(context).get(new TransactionsSQL.TransactionsCallback() {
            @Override
            public void onSuccess(Transactions transactionsList) {
                transactions.postValue(transactionsList);
                dataLoading.postValue(false);
            }

            @Override
            public void onFailure(int errorCode) {
                transactions.postValue(new Transactions());
                dataLoading.postValue(false);
            }
        });
        /*executorService.submit(() -> {
            Transactions transactions = Controller.transactions(context).get();
            this.transactions.postValue(transactions);
            dataLoading.postValue(false);
        });*/
    }

    // Leer un mes completo
    public void loadTransactions(final Context context, final Date date) {
        //final ExecutorService executorService = Executors.newSingleThreadExecutor();
        initDate = date;
        endDate = date;
        dataLoading.setValue(true);
        Controller.transactions(context).get(date, new TransactionsSQL.TransactionsCallback() {
            @Override
            public void onSuccess(Transactions transactionsList) {
                Log.d("LOAD_TRANSACTION", "TransactionsViewModel leyó " + transactionsList.length());
                transactions.postValue(transactionsList);
                dataLoading.postValue(false);
            }

            @Override
            public void onFailure(int errorCode) {
                Log.d("LOAD_TRANSACTION", "Ocurrió un error en TransactionsViewModel " + errorCode);
                transactions.postValue(null);
                dataLoading.postValue(false);
            }
        });
        /*executorService.submit(() -> {
            Transactions transactions = Controller.transactions(context).get(date.getYear(), date.getMonth());
            this.transactions.postValue(transactions);
            dataLoading.postValue(false);
        });*/
    }

    public void removeData() {
        transactions = new MutableLiveData<>();
        dataLoading = new MutableLiveData<>();
    }

    public boolean isSameDate(final Date initDate, final Date endDate) {
        return this.initDate.isSameDay(initDate) && this.endDate.isSameDay(endDate);
    }

    public boolean isSameDate(final Date date) {
        return this.initDate.isSameDay(date) && this.endDate.isSameDay(date);
    }
}