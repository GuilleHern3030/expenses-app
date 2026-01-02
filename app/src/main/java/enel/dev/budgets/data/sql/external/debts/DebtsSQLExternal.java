package enel.dev.budgets.data.sql.external.debts;


import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.sql.BasicSQL;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.data.sql.UserController;
import enel.dev.budgets.data.sql.external.ApiClient;
import enel.dev.budgets.objects.debt.Debt;
import enel.dev.budgets.objects.debt.Debts;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.data.sql.local.DebtsSQL;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DebtsSQLExternal extends enel.dev.budgets.data.sql.DebtsSQL {

    public DebtsSQLExternal(final Context context, final String dbName) {
        super(context, dbName);
    }

    public static void createDefaultTables(@NonNull Context context, @NonNull BasicSQL sql, Debt[] debts) {
        final boolean tableCreated = sql.tablaCrear(DEBTS_TABLE, new String[]{
                "id",
                "lendername",
                "coinname",
                "coinamount",
                "description"
        });
        if (tableCreated) {
            for (Debt debt : debts) {
                sql.tablaIngresarFila(DEBTS_TABLE, new String[]{
                        String.valueOf(debt.id()), // id
                        debt.getLender(), // lendername
                        debt.getMoney().getCoin().getName(), // coinname
                        String.valueOf(debt.getMoney().getAmount()), // coinamount
                        debt.getDescription() // description
                });
            }
        }
    }

    /**
     * Obtener todas las deudas existentes
     * Las columnas son:
     *          0- id
     *          1- lendername
     *          2- coinname
     *          3- coinamount
     *          4- description
     * @return
     */
    @Override
    public Debts get() {
        return new DebtsSQL(context, DATA_BASE).get();
    }

    @Override
    public void add(final Debt rawData, final Controller.SQLcallback callback) {

        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.post(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                Body.create(rawData)
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

                final Debt debt = Body.recreate(rawData, body.id);
                if (debt.id() <= 0) {
                    callback.onError("Debt null");
                    return;
                }

                new DebtsSQL(context, DATA_BASE).add(debt, callback);

            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });




    }

    @Override
    public void edit(final Debt debt, final Controller.SQLcallback callback) {

        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.put(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                Body.create(debt)
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

                final Debt transaction = Body.recreate(debt, body.id);
                if (transaction.id() <= 0) {
                    callback.onError("Debt null");
                    return;
                }

                new DebtsSQL(context, DATA_BASE).edit(debt, callback);
            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });



    }

    @Override
    public void delete(final int id, final Controller.SQLcallback callback) {

        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.delete(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                id
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

                new DebtsSQL(context, DATA_BASE).delete(id, callback);
            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });

    }

    @Override
    public void deleteCoin(final Coin coin) {
        new DebtsSQL(context, DATA_BASE).deleteCoin(coin);
    }

    @Override
    public void editCoin(final Coin oldCoin, final Coin newCoin) {
        new DebtsSQL(context, DATA_BASE).editCoin(oldCoin, newCoin);
    }

}
