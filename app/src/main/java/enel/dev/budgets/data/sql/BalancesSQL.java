package enel.dev.budgets.data.sql;

import static enel.dev.budgets.data.sql.Controller.DATA_BASE_LOCAL;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.objects.money.Balance;
import enel.dev.budgets.objects.money.Coin;

public abstract class BalancesSQL {

    protected static final String BALANCE_TABLE = "BALANCE";

    protected final Context context;
    protected final String DATA_BASE;

    public BalancesSQL(final Context context, final String dbName) {
        this.context = context;
        this.DATA_BASE = dbName;
    }

    public static void createDefaultTables(@NonNull BasicSQL sql, final Activity activity) {
        final boolean tableCreated = sql.tablaCrear(BALANCE_TABLE, new String[]{
                "coinname",
                "coinsymbol"
        });
        if (tableCreated && activity != null) {
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
    public abstract Balance get();

    public abstract void add(final Coin coin, final Controller.SQLcallback callback);

    public abstract void edit(final String oldCoinName, final Coin coin, final Controller.SQLcallback callback);

    public abstract void delete(final Coin coin, final Controller.SQLcallback callback);

    public static boolean setDefaultCoin(final Activity context, final String coinSymbol) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE_LOCAL);
        String[] data = sql.tablaObtenerFila(BALANCE_TABLE, 0);
        boolean success = sql.tablaEditarFila(BALANCE_TABLE, 0, new String[]{
                "", // coinname
                coinSymbol // coinsymbol
        });

        sql.listarTablas();
        sql.cerrar();

        if (success) Preferences.setDefaultCoin(context, coinSymbol);
        return success;
    }
}
