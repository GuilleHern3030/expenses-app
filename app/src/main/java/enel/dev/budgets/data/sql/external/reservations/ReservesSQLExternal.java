package enel.dev.budgets.data.sql.external.reservations;


import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.sql.BasicSQL;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.data.sql.UserController;
import enel.dev.budgets.data.sql.external.ApiClient;
import enel.dev.budgets.data.sql.local.ReservesSQL;
import enel.dev.budgets.objects.reserve.Reserve;
import enel.dev.budgets.objects.reserve.Reserves;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservesSQLExternal extends enel.dev.budgets.data.sql.ReservesSQL {

    public ReservesSQLExternal(final Context context, final String dbName) {
        super(context, dbName);
    }

    public static void createDefaultTables(@NonNull Context context, @NonNull BasicSQL sql, Reserve[] reserves) {
        final boolean tableCreated = sql.tablaCrear(RESERVES_TABLE, new String[]{
                "id",
                "coinname",
                "coinamount"
        });
        if (tableCreated) {
            for (Reserve debt : reserves) {
                sql.tablaIngresarFila(RESERVES_TABLE, new String[]{
                        String.valueOf(debt.id()), // id
                        debt.getName(), // name
                        String.valueOf(debt.getAmount()) // amount
                });
            }
        }
    }

    /**
     * Obtener todas las deudas existentes
     * Las columnas son:
     *          0- id
     *          1- name
     *          2- amount
     * @return {Reserves} Conjunto de reservas
     */
    @Override
    public Reserves get() {
        return new ReservesSQL(context, DATA_BASE).get();
    }

    @Override
    public void add(final Reserve rawData, final Controller.SQLcallback callback) {

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

                final Reserve debt = Body.recreate(rawData, body.id);
                if (debt.id() <= 0) {
                    callback.onError("Debt null");
                    return;
                }

                new ReservesSQL(context, DATA_BASE).add(debt, callback);

            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });




    }

    @Override
    public void edit(final Reserve debt, final Controller.SQLcallback callback) {

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

                final Reserve transaction = Body.recreate(debt, body.id);
                if (transaction.id() <= 0) {
                    callback.onError("Debt null");
                    return;
                }

                new ReservesSQL(context, DATA_BASE).edit(debt, callback);
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

                new ReservesSQL(context, DATA_BASE).delete(id, callback);
            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });

    }

}
