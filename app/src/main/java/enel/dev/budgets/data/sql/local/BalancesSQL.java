package enel.dev.budgets.data.sql.local;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.data.sql.BasicSQL;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.money.Balance;
import enel.dev.budgets.objects.money.Coin;

public class BalancesSQL extends enel.dev.budgets.data.sql.BalancesSQL {

    public BalancesSQL(final Context context, final String dbName) {
        super(context, dbName);
    }

    public static void createDefaultTables(@NonNull Activity activity, @NonNull BasicSQL sql) {
        final boolean tableCreated = sql.tablaCrear(BALANCE_TABLE, new String[]{
                "coinname",
                "coinsymbol"
        });
        if (tableCreated) {
            sql.tablaIngresarFila(BALANCE_TABLE, new String[]{
                    Preferences.defaultCoin(activity).getName(), // coinname
                    Preferences.defaultCoin(activity).getSymbol() // coinsymbol
            });
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
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        Balance balance = get(sql);
        sql.cerrar();
        return balance;
    }

    static Balance get(@NonNull BasicSQL sql) {
        final Balance balance = new Balance();
        try {
            final int coins = sql.tablaFilas(BALANCE_TABLE);
            for (int row = 0; row < coins; row++) {
                String[] columns = sql.tablaObtenerFila(BALANCE_TABLE, row);
                balance.add(new Coin(columns[0], columns[1]));
            }
        } catch(Exception ignored) { }
        return balance;
    }

    @Override
    public void add(final Coin coin, final Controller.SQLcallback callback) {
        if (coin == null) { callback.onError("Invalid coin"); return; }
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        Balance balance = get(sql);
        if (balance.exists(coin.getName())) { callback.onError("Invalid coin"); return; }
        final int success = sql.tablaIngresarFila(BALANCE_TABLE, new String[]{
                coin.getName(),
                coin.getSymbol()
        });
        sql.cerrar();
        if (success >= 0)
            callback.onSuccess();
        else callback.onError("");
    }

    @Override
    public void edit(final String oldCoinName, final Coin coin, final Controller.SQLcallback callback) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        if (coin != null) try {
            final int row = sql.tablaBuscarFila(BALANCE_TABLE, "coinname", oldCoinName, false);
            if (row >= 0) {
                final boolean success = sql.tablaEditarFila(BALANCE_TABLE, row, new String[]{
                        coin.getName(),
                        coin.getSymbol()
                });
                if (success)
                    callback.onSuccess();
                else callback.onError("");
            }
        } catch (Exception ignored) { }
        sql.cerrar();
        callback.onError("");
    }

    @Override
    public void delete(final Coin coin, final Controller.SQLcallback callback) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        if (coin != null) try {
            final int row = sql.tablaBuscarFila(BALANCE_TABLE, "coinname", coin.getName(), false);
            if (row >= 0) {
                final boolean success = sql.tablaEliminarFila(BALANCE_TABLE, row, true);
                if (success) callback.onSuccess();
                else callback.onError("");
            }
        } catch (Exception ignored) { }
        sql.cerrar();
        callback.onError("");
    }

    /*public static boolean setDefaultCoin(final Activity context, final String coinSymbol) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        String[] data = sql.tablaObtenerFila(BALANCE_TABLE, 0);
        boolean success = sql.tablaEditarFila(BALANCE_TABLE, 0, new String[]{
                "", // coinname
                coinSymbol // coinsymbol
        });

        sql.listarTablas();
        sql.cerrar();

        if (success) Preferences.setDefaultCoin(context, coinSymbol);
        return success;
    }*/
}
