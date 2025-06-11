package enel.dev.budgets.data.sql;

import android.app.Activity;
import android.content.Context;

/**
 *  Permite acceder a las bases de datos SQL
 */
public class Controller {

    static final String DATA_BASE = "DATABASE";

    /**
     * Crea todas las tablas requeridas para la Base de Datos
     * @param context context
     */
    public static void loadDefaultData(final Activity context) {
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        BalancesSQL.createDefaultTables(context, sql);
        CategoriesSQL.createDefaultTables(context, sql);
        DebtsSQL.createDefaultTables(sql);
        ShoppingListSQL.createDefaultTables(sql);
        sql.cerrar();
    }

    public static BalancesSQL balances(final Context context) {
        return new BalancesSQL(context);
    }

    public static CategoriesSQL categories(final Context context) {
        return new CategoriesSQL(context);
    }

    public static TransactionsSQL transactions(final Context context) {
        return new TransactionsSQL(context);
    }

    public static DebtsSQL debts(final Context context) {
        return new DebtsSQL(context);
    }

    public static ShoppingListSQL shoppingList(final Context context) {
        return new ShoppingListSQL(context);
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

}
