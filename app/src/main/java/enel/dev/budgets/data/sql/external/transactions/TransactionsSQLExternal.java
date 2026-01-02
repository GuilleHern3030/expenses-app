package enel.dev.budgets.data.sql.external.transactions;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.sql.BasicSQL;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.data.sql.UserController;
import enel.dev.budgets.data.sql.external.ApiClient;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;
import enel.dev.budgets.data.sql.local.TransactionsSQL;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Las columnas son:
 *          0- id
 *          1- coinname: nombre de la moneda
 *          2- coinsymbol: simbolo de la moneda
 *          3- amount: cantidad de dinero (double)
 *          4- date (encoded)
 *          5- category: nombre de la categoría
 *          6- isincome: es un 'income'? (1 para sí)
 *          7- description: descripcion de la transaccion
 *          8- photouri: uri de la foto relacionada
 */
public class TransactionsSQLExternal extends enel.dev.budgets.data.sql.TransactionsSQL {

    private final static String TABLE_NAME = "PULLED_TRANSACTIONS";

    public TransactionsSQLExternal(final Context context, final String dbName) {
        super(context, dbName);
    }

    @Override
    public void get(int year, int month, TransactionsCallback callback) {
        try {
            final Date date = new Date(year, month, 1);
            if (!date.isSameMonth(new Date())) {
                Log.d("LOAD_TRANSACTION", "El mes que vamos a cargar no es el actual, por lo que se cargará desde backend");
                get(date, new Date(year, month, Date.maxDay(month, year)), callback);
            } else {
                Transactions transactions = new enel.dev.budgets.data.sql.local.TransactionsSQL(context, DATA_BASE).get(TABLE_NAME);
                callback.onSuccess(transactions);
            }
        } catch (Exception e) { callback.onFailure(500); }
    }

    @Override
    public void get(@NonNull final Date initDate, @NonNull final Date endDate, final TransactionsCallback callback) {
        try {
            if (initDate.isSameMonth(endDate) && initDate.isSameMonth(new Date())) {
                final Transactions transactions = new enel.dev.budgets.data.sql.local.TransactionsSQL(context, DATA_BASE).get(TABLE_NAME);
                final Transactions filteredTransactions = transactions.filterDate(initDate, endDate);
                callback.onSuccess(filteredTransactions);
            } else {
                Log.d("LOAD_TRANSACTION", "Cargando desde backend...");

                final Request.Service api = ApiClient.getClient().create(Request.Service.class);
                api.select(
                        UserController.getID(context),
                        UserController.getUUID(context),
                        UserController.getSyncCode(context),
                        "Bearer " + UserController.getToken(context),
                        Build.MANUFACTURER + " " + Build.MODEL,
                        String.valueOf(initDate.partialEncode()),
                        String.valueOf(endDate.partialEncode())
                ).enqueue(new Callback<Request.SelectResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<Request.SelectResponse> call, @NonNull Response<Request.SelectResponse> response) {

                        if (!response.isSuccessful()) { // si response.code() no está entre 200–299
                            callback.onFailure(response.code());
                            return;
                        }

                        if (response.body() != null) try {
                            final Transactions transactions = Body.recreate(response.body().transactions);
                            callback.onSuccess(transactions);
                        } catch (Exception e) {
                            Log.e("LOAD_TRANSACTION", e.toString(), e);
                            callback.onFailure(415);
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<Request.SelectResponse> call, @NonNull Throwable t) {
                        callback.onFailure(503); // Network error
                        Log.e("LOAD_TRANSACTION", t.toString(), t);
                    }
                });


            }
        } catch (Exception e) { callback.onFailure(500); }
    }

    public static void createTransactionsTable(@NonNull Context context, @NonNull BasicSQL sql, Transaction[] transactions) {
        final boolean tableCreated = sql.tablaCrear(TABLE_NAME, new String[]{
                "id",
                "coinname",
                "coinsymbol",
                "amount",
                "date",
                "category",
                "isanincome",
                "description",
                "photouri"
        });
        if (tableCreated) {
            for (Transaction transaction : transactions) {
                String[] transactionData = parseData(transaction);
                sql.tablaIngresarFila(TABLE_NAME, transactionData);
            }
        }
    }

    @Override
    public void add(final Transaction rawData, final TransactionCallback callback) {

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

                final Transaction transaction = Body.recreate(rawData, body.id);
                if (transaction.id() <= 0) {
                    callback.onError("Transaction null");
                    return;
                }

                if (transaction.getDate().isSameMonth(new Date())) {
                    final BasicSQL sql = new BasicSQL(context, DATA_BASE);
                    String[] data = parseData(transaction);
                    final int newRow = sql.tablaIngresarFila(TABLE_NAME, data);
                    sql.cerrar();
                    if (newRow >= 0) callback.onSuccess(transaction);
                    else callback.onError("Invalid Transaction");
                } else callback.onSuccess(transaction);
            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });
    }

    @Override
    public void edit(final Transaction oldTransaction, final Transaction newTransaction, final TransactionCallback callback) {

        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.put(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                Body.create(newTransaction, oldTransaction.id())
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

                final Transaction transaction = Body.recreate(newTransaction, body.id);
                if (transaction.id() <= 0) {
                    callback.onError("Transaction null");
                    return;
                }

                if (transaction.getDate().isSameMonth(new Date())) {
                    final BasicSQL sql = new BasicSQL(context, DATA_BASE);
                    String[] data = parseData(transaction);

                    final int row = sql.tablaBuscarFila(TABLE_NAME, "id", String.valueOf(transaction.id()), true);

                    boolean success = false;
                    if (row >= 0) success = sql.tablaEditarFila(TABLE_NAME, row, data);
                    else success = sql.tablaIngresarFila(TABLE_NAME, data) >= 0;

                    sql.cerrar();

                    if (!success) {
                        callback.onError("Invalid Transaction");
                        return;
                    }
                }

                else if (oldTransaction.getDate().isSameMonth(new Date())) {
                    final BasicSQL sql = new BasicSQL(context, DATA_BASE);
                    final int row = sql.tablaBuscarFila(TABLE_NAME, "id", String.valueOf(oldTransaction.id()), true);
                    sql.tablaEliminarFila(TABLE_NAME, row, true);
                    sql.cerrar();
                }

                callback.onSuccess(transaction);
            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });
    }

    @Override
    public void delete(final int id, final String dateEncoded, final Controller.SQLcallback callback) {

        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.delete(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                id,
                dateEncoded
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

                try {
                    if (new Date(dateEncoded).isSameMonth(new Date())) {
                        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
                        final int row = sql.tablaBuscarFila(TABLE_NAME, "id", String.valueOf(id), true);
                        sql.tablaEliminarFila(TABLE_NAME, row, true);
                        sql.cerrar();
                    }
                } catch (Exception ignored) { }

                callback.onSuccess();
            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });
    }

    @Override
    public void deleteCoin(final Coin coin) {
        new TransactionsSQL(context, DATA_BASE).deleteCoin(coin);
    }

    @Override
    public void editCoin(final Coin oldCoin, final Coin newCoin) {
        new TransactionsSQL(context, DATA_BASE).editCoin(oldCoin, newCoin);
    }

    private static String[] parseData(Transaction transaction) {
        return new String[]{
                String.valueOf(transaction.id()), // ID
                transaction.getMoney().name(), // coin name
                transaction.getMoney().getCoin().getSymbol(), // coin symbol
                String.valueOf(transaction.getMoney().getAmount()), // amount
                transaction.getDate().toString(), // date encoded
                transaction.getCategory().getName(), // category name
                transaction.isAnIncome() ? "1" : "0", // isAnIncome
                transaction.getDescription(), // description
                transaction.getPhotoUri()
        };
    }

}
