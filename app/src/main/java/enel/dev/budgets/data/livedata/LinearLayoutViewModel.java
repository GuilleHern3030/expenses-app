package enel.dev.budgets.data.livedata;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transactions;
import enel.dev.budgets.objects.transaction.TransactionsArray;
import enel.dev.budgets.views.home.TransactionsListLayout;
import enel.dev.budgets.views.summary.SummaryListLayout;
import enel.dev.budgets.views.summary.TransactionsByDayListLayout;

public class LinearLayoutViewModel extends ViewModel {
    private MutableLiveData<Boolean> inflating;
    private MutableLiveData<View> linearLayout;

    private Date date;

    public LinearLayoutViewModel() {
        inflating = new MutableLiveData<>();
        linearLayout = new MutableLiveData<>();
    }

    public LiveData<Boolean> isInflating() {
        return inflating;
    }

    public LiveData<View> getLayout() {
        return linearLayout;
    }

    private View getView(final Context context, final ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listview_preloaded_transactions, parent, false);
    }

    public void showTransactions(final Context context, final TransactionsByDayListLayout transactionsByDayListLayout, final Transactions transactions) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final View view = getView(context, transactionsByDayListLayout.getLayout());
        final LinearLayout layout = view.findViewById(R.id.linear_layout);
        inflating.setValue(true);
        executorService.submit(() -> {
            transactionsByDayListLayout.showContent(layout, new TransactionsArray(transactions));
            linearLayout.postValue(layout);
            inflating.postValue(false);
        });
    }

    public void showTransactions(final Context context, final TransactionsListLayout transactionsListLayout, final Transactions transactions) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final View view = getView(context, transactionsListLayout.getLayout());
        final LinearLayout layout = view.findViewById(R.id.linear_layout);
        inflating.setValue(true);
        executorService.submit(() -> {
            transactionsListLayout.showContent(layout, transactions);
            linearLayout.postValue(layout);
            inflating.postValue(false);
        });
    }

    public void showTransactions(final Context context, final SummaryListLayout summaryListLayout, final Transactions transactions, final Money total) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final View view = getView(context, summaryListLayout.getLayout());
        final LinearLayout layout = view.findViewById(R.id.linear_layout);
        inflating.setValue(true);
        executorService.submit(() -> {
            summaryListLayout.showContent(layout, transactions, total);
            linearLayout.postValue(layout);
            inflating.postValue(false);
        });
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public boolean isCurrentDate(final Date date) {
        return this.date == date;
    }

    public void detach() {
        inflating = null;
        linearLayout = null;
    }
}