package enel.dev.budgets.data.sql.external.shoppinglist;

import static enel.dev.budgets.data.sql.Controller.DATA_BASE_EXTERNAL;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.sql.BasicSQL;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.data.sql.UserController;
import enel.dev.budgets.data.sql.external.ApiClient;
import enel.dev.budgets.objects.shoppinglist.Item;
import enel.dev.budgets.objects.shoppinglist.ShoppingList;
import enel.dev.budgets.objects.shoppinglist.ShoppingListArray;
import enel.dev.budgets.data.sql.local.ShoppingListSQL;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShoppingListSQLExternal extends enel.dev.budgets.data.sql.ShoppingListSQL {

    private static String DEFAULT_SHOPPING_LIST = "Shopping";

    public ShoppingListSQLExternal(final Context context, final String dbName) {
        super(context, dbName);
    }

    public static void createDefaultTables(@NonNull Context context, @NonNull BasicSQL sql, Item[] items) {
        sql.tablaCrear(SHOPPING_LIST_TABLE, new String[]{
                "listname", // data[0]
                "listcontent" // data[1]
        });
        ShoppingListSQL tSQL = new ShoppingListSQL(context, DATA_BASE_EXTERNAL);
        tSQL.add(new ShoppingList(DEFAULT_SHOPPING_LIST));
        for (Item item : items)
            enel.dev.budgets.data.sql.local.ShoppingListSQL.add(sql, DEFAULT_SHOPPING_LIST, item);
    }

    @Override
    public ShoppingList get() {
        return new ShoppingListSQL(context, DATA_BASE).get();
    }

    @Override
    public ShoppingList get(final String listName) {
        return new ShoppingListSQL(context, DATA_BASE).get(listName);
    }

    @Override
    public ShoppingListArray getShoppingListArray() {
        return new ShoppingListSQL(context, DATA_BASE).getShoppingListArray();
    }

    @Override
    public void add(final String shoppingListName, final Item item, final Controller.SQLcallback callback) {

        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.post(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                item.getName()
        ).enqueue(new Callback<Request.Response>() {
            @Override
            public void onResponse(@NonNull Call<Request.Response> call, @NonNull Response<Request.Response> response) {

                if (!response.isSuccessful()) { // si response.code() no está entre 200–299
                    callback.onError(response.message());
                    return;
                }

                Request.Response body = response.body();
                if (body == null) {
                    callback.onError("Empty response");
                    return;
                }

                new ShoppingListSQL(context, DATA_BASE).add(shoppingListName, item, callback);

            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });
    }

    @Override
    public void edit(final String shoppingListName, final String itemName, final Item item, final Controller.SQLcallback callback) {



        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.put(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                item.getName(),
                itemName
        ).enqueue(new Callback<Request.Response>() {
            @Override
            public void onResponse(@NonNull Call<Request.Response> call, @NonNull Response<Request.Response> response) {

                if (!response.isSuccessful()) { // true si response.code() está entre 200–299
                    callback.onError(response.message());
                    return;
                }

                Request.Response body = response.body();
                if (body == null) {
                    callback.onError("Empty response");
                    return;
                }

                new ShoppingListSQL(context, DATA_BASE).edit(shoppingListName, itemName, item, callback);
            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });

    }

    @Override
    public void remove(final String shoppingListName, final String itemName, final Controller.SQLcallback callback) {

        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.delete(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                itemName
        ).enqueue(new Callback<Request.Response>() {
            @Override
            public void onResponse(@NonNull Call<Request.Response> call, @NonNull Response<Request.Response> response) {

                if (!response.isSuccessful()) { // true si response.code() está entre 200–299
                    callback.onError(response.message());
                    return;
                }

                new ShoppingListSQL(context, DATA_BASE).remove(shoppingListName, itemName, callback);
            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });

    }

    @Override
    public boolean add(final ShoppingList shoppingList) {
        return false; // No se pueden agregar listas extras
    }

    @Override
    public boolean remove(final String shoppingListName) {
        return false; // No se pueden eliminar listas
    }

    @Override
    public boolean edit(final String listName, final ShoppingList shoppingList) {
        return false; // No se pueden editar listas extras
    }

    @Override
    public void setFirst(final String listName) {
        new ShoppingListSQL(context, DATA_BASE).setFirst(listName);
    }

    @Override
    public void setChecked(String shoppingListName, String itemName, boolean checked) {
        new ShoppingListSQL(context, DATA_BASE).setChecked(DEFAULT_SHOPPING_LIST, itemName, checked);
    }

}
