package enel.dev.budgets.data.sql.external.balances;

import static enel.dev.budgets.data.sql.Controller.DATA_BASE_EXTERNAL;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.data.sql.BasicSQL;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.data.sql.UserController;
import enel.dev.budgets.data.sql.external.ApiClient;
import enel.dev.budgets.objects.money.Balance;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.data.sql.local.BalancesSQL;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BalancesSQLExternal extends enel.dev.budgets.data.sql.BalancesSQL {

    public BalancesSQLExternal(final Context context, final String dbName) {
        super(context, dbName);
    }

    public static void createDefaultTables(@NonNull Activity activity, @NonNull BasicSQL sql, Coin[] coins) {
        final boolean tableCreated = sql.tablaCrear(BALANCE_TABLE, new String[]{
                "coinname",
                "coinsymbol"
        });

        if (tableCreated) {
            sql.tablaIngresarFila(BALANCE_TABLE, new String[]{
                    Preferences.defaultCoin(activity).getName(), // coinname
                    Preferences.defaultCoin(activity).getSymbol() // coinsymbol
            });
            for (Coin coin : coins) {
                sql.tablaIngresarFila(BALANCE_TABLE, new String[]{
                        coin.getName(),
                        coin.getSymbol()
                });
            }
        }
    }

    /**
     * Obtiene la totalidad de las monedas guardadas en la tabla BALANCE_TABLE.
     * Las columnas son:
     *          0- coinname: nombre de la moneda
     *          1- coinsymbol: simbolo de la moneda
     * @return Devuelve un conjunto de objetos Money.
     */
    @Override
    public Balance get() {
        return new BalancesSQL(context, DATA_BASE).get();
    }

    @Override
    public void add(final Coin coin, final Controller.SQLcallback callback) {

        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.post(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                Body.create(coin)
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

                new BalancesSQL(context, DATA_BASE).add(coin, callback);

            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });
    }

    @Override
    public void edit(final String oldCoinName, final Coin coin, final Controller.SQLcallback callback) {


        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.put(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                Body.create(coin, oldCoinName)
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

                new BalancesSQL(context, DATA_BASE).edit(oldCoinName, coin, callback);

            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });

    }

    @Override
    public void delete(final Coin coin, final Controller.SQLcallback callback) {


        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.delete(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                coin.getName(),
                coin.getSymbol()
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

                new BalancesSQL(context, DATA_BASE).delete(coin, callback);

            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });

    }
}
