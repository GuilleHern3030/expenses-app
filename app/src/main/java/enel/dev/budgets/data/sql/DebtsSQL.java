package enel.dev.budgets.data.sql;

import android.content.Context;

import androidx.annotation.NonNull;

import enel.dev.budgets.objects.debt.Debt;
import enel.dev.budgets.objects.debt.Debts;
import enel.dev.budgets.objects.money.Coin;

public abstract class DebtsSQL {

    protected static final String DEBTS_TABLE = "DEBTS";

    protected final Context context;
    protected final String DATA_BASE;

    public DebtsSQL(final Context context, final String dbName) {
        this.context = context;
        this.DATA_BASE = dbName;
    }

    static void createDefaultTables(@NonNull BasicSQL sql) {
        sql.tablaCrear(DEBTS_TABLE, new String[]{
                "id",
                "lendername",
                "coinname",
                "coinamount",
                "description"
        });
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
    public abstract Debts get();

    public abstract void add(final Debt debt, final Controller.SQLcallback callback);

    public abstract void edit(final Debt debt, final Controller.SQLcallback callback);

    public abstract void delete(final int id, final Controller.SQLcallback callback);

    public abstract void deleteCoin(final Coin coin);

    public abstract void editCoin(final Coin oldCoin, final Coin newCoin);

}
