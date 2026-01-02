package enel.dev.budgets.data.sql;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.external.reservations.ReservesSQLExternal;
import enel.dev.budgets.data.sql.external.synchronize.Pull;
import enel.dev.budgets.data.sql.external.balances.BalancesSQLExternal;
import enel.dev.budgets.data.sql.external.categories.CategoriesSQLExternal;
import enel.dev.budgets.data.sql.external.debts.DebtsSQLExternal;
import enel.dev.budgets.data.sql.external.shoppinglist.ShoppingListSQLExternal;
import enel.dev.budgets.data.sql.external.transactions.TransactionsSQLExternal;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.debt.Debt;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.reserve.Reserve;
import enel.dev.budgets.objects.shoppinglist.Item;
import enel.dev.budgets.objects.transaction.Transaction;

/**
 *  Permite acceder a las bases de datos SQL
 */
public class Controller {

    private final static int MINUTES = 5;

    public interface SQLcallback {
        void onSuccess();
        void onError(final String error);
        void onNetworkError();
    }

    public interface LoadCallback {
        void onSuccess();
        void onError(final String errorMessage, final int errorCode);
        void onNetworkError();
    }

    public static final String DATA_BASE_LOCAL = "DATABASE";
    public static final String DATA_BASE_EXTERNAL = "DATABASEEXT";

    /**
     * Crea todas las tablas requeridas para la Base de Datos
     * @param context context
     */
    public static void loadDefaultData(final Activity context) {
        final BasicSQL sql = new BasicSQL(context, DATA_BASE_LOCAL);
        BalancesSQL.createDefaultTables(sql, context);
        CategoriesSQL.createDefaultTables(sql, context);
        DebtsSQL.createDefaultTables(sql);
        ReservesSQL.createDefaultTables(sql);
        ShoppingListSQL.createDefaultTables(sql);
        UserController.createDefaultTables(sql);
        sql.cerrar();
    }

    public static void loadDataBase(final Activity context, final LoadCallback callback) {
        loadDataBase(context, callback, false);
    }

    public static void loadDataBase(final Activity context, final LoadCallback callback, final boolean force) {
        try {
            if (UserController.useCloud(context)) {
                if (force || hasPassedMinutes(UserController.lastPull(context))) {
                    Pull.load(context,
                            new Pull.PullCallback() {

                                @Override
                                public void onSuccess(Coin[] balance, Category[] categories, Debt[] debts, Item[] shoppingListItems, Transaction[] transactions, Reserve[] reserves, Date date) {

                                    Log.d("PULL_SUCCESS", "[BALANCE] " + Arrays.toString(balance));
                                    Log.d("PULL_SUCCESS", "[CATEGORY] " + Arrays.toString(categories));
                                    Log.d("PULL_SUCCESS", "[DEBT] " + Arrays.toString(debts));
                                    Log.d("PULL_SUCCESS", "[RESERVE] " + Arrays.toString(reserves));
                                    Log.d("PULL_SUCCESS", "[SHOPPINGLISTITEMS] " + Arrays.toString(shoppingListItems));
                                    Log.d("PULL_SUCCESS", "[TRANSACTIONS] " + Arrays.toString(transactions));

                                    BasicSQL.eliminar(context, DATA_BASE_EXTERNAL);
                                    final BasicSQL sql = new BasicSQL(context, DATA_BASE_EXTERNAL);
                                    BalancesSQLExternal.createDefaultTables(context, sql, balance);
                                    CategoriesSQLExternal.createDefaultTables(context, sql, categories);
                                    DebtsSQLExternal.createDefaultTables(context, sql, debts);
                                    ReservesSQLExternal.createDefaultTables(context, sql, reserves);
                                    ShoppingListSQLExternal.createDefaultTables(context, sql, shoppingListItems);
                                    TransactionsSQLExternal.createTransactionsTable(context, sql, transactions);
                                    UserController.updateLastPull(sql);
                                    sql.cerrar();

                                    callback.onSuccess();
                                }

                                @Override
                                public void onError(final int errorCode) {
                                    if (errorCode == 401) callback.onError(context.getString(R.string.synchronize_code_error), errorCode);
                                    else if (errorCode == 403) callback.onError(context.getString(R.string.sync_pull_no_data), errorCode);
                                    else callback.onError(context.getString(R.string.sync_pull_server_error), errorCode);
                                    Log.e("PULL_ERROR", String.valueOf(errorCode));
                                }

                                @Override
                                public void onNetworkError() {
                                    callback.onNetworkError();
                                }
                            });
                } else callback.onSuccess(); // si no pasaron más de X minutos desde el último PULL, no actualiza
            } else callback.onSuccess(); // si no está configurado usar la nube
        } catch (Exception e) { callback.onNetworkError(); }
    }

    public static BalancesSQL balances(final Context context) {
        if (!UserController.useCloud(context))
            return new enel.dev.budgets.data.sql.local.BalancesSQL(context, DATA_BASE_LOCAL);
        else return new BalancesSQLExternal(context, DATA_BASE_EXTERNAL);
    }

    public static CategoriesSQL categories(final Context context) {
        if (!UserController.useCloud(context))
            return new enel.dev.budgets.data.sql.local.CategoriesSQL(context, DATA_BASE_LOCAL);
        else return new CategoriesSQLExternal(context, DATA_BASE_EXTERNAL);
    }

    public static enel.dev.budgets.data.sql.local.TransactionsSQL localTransactions(final Context context) {
        return new enel.dev.budgets.data.sql.local.TransactionsSQL(context, DATA_BASE_LOCAL);
    }

    public static TransactionsSQL transactions(final Context context) {
        if (!UserController.useCloud(context))
            return new enel.dev.budgets.data.sql.local.TransactionsSQL(context, DATA_BASE_LOCAL);
        else return new TransactionsSQLExternal(context, DATA_BASE_EXTERNAL);
    }

    public static DebtsSQL debts(final Context context) {
        if (!UserController.useCloud(context))
            return new enel.dev.budgets.data.sql.local.DebtsSQL(context, DATA_BASE_LOCAL);
        else return new DebtsSQLExternal(context, DATA_BASE_EXTERNAL);
    }

    public static ReservesSQL reserves(final Context context) {
        if (!UserController.useCloud(context))
            return new enel.dev.budgets.data.sql.local.ReservesSQL(context, DATA_BASE_LOCAL);
        else return new ReservesSQLExternal(context, DATA_BASE_EXTERNAL);
    }

    public static ShoppingListSQL shoppingList(final Context context) {
        if (!UserController.useCloud(context))
            return new enel.dev.budgets.data.sql.local.ShoppingListSQL(context, DATA_BASE_LOCAL);
        else return new ShoppingListSQLExternal(context, DATA_BASE_EXTERNAL);
    }

    /*public static boolean isMoreThanOneCurrency(@Nullable Context context) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final int coins = sql.tablaFilas(BALANCE_TABLE);
        sql.cerrar();
        return coins > 1;
    }*/

    public static boolean setDefaultCoin(final Activity context, final String coinSymbol) {
        return BalancesSQL.setDefaultCoin(context, coinSymbol);
    }

    public static void deleteAllData(final Context context) {
        BasicSQL.eliminarTodasLasBasesDeDatos(context);
    }

    private static boolean hasPassedMinutes(long lastPull) {
        long now = System.currentTimeMillis();
        long diffMillis = now - lastPull;
        long diffMinutes = diffMillis / (60 * 1000);
        return diffMinutes >= Controller.MINUTES;
    }


}
