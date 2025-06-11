package enel.dev.budgets.data.livedata;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.data.sql.ShoppingListSQL;
import enel.dev.budgets.objects.shoppinglist.ShoppingListArray;

public class ShoppingListsViewModel extends ViewModel {
    private final MutableLiveData<ShoppingListArray> shoppingLists;
    private final MutableLiveData<Boolean> dataLoading;
    private final ExecutorService executorService;


    public ShoppingListsViewModel() {
        shoppingLists = new MutableLiveData<>();
        dataLoading = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<ShoppingListArray> getLists() {
        return shoppingLists;
    }

    public LiveData<Boolean> isDataLoading() {
        return dataLoading;
    }

    public void loadShoppingLists(final Context context) {
        dataLoading.setValue(true);
        executorService.submit(() -> {

            ShoppingListArray listsLoaded = Controller.shoppingList(context).getShoppingListArray();

            shoppingLists.postValue(listsLoaded);

            dataLoading.postValue(false);
        });
    }
}