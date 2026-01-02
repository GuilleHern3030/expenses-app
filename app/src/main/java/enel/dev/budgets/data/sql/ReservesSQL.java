package enel.dev.budgets.data.sql;

import android.content.Context;

import androidx.annotation.NonNull;

import enel.dev.budgets.objects.debt.Debt;
import enel.dev.budgets.objects.debt.Debts;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.reserve.Reserve;
import enel.dev.budgets.objects.reserve.Reserves;

public abstract class ReservesSQL {

    protected static final String RESERVES_TABLE = "RESERVES";

    protected final Context context;
    protected final String DATA_BASE;

    public ReservesSQL(final Context context, final String dbName) {
        this.context = context;
        this.DATA_BASE = dbName;
    }

    static void createDefaultTables(@NonNull BasicSQL sql) {
        sql.tablaCrear(RESERVES_TABLE, new String[]{
                "id",
                "name",
                "amount"
        });
    }

    /**
     * Obtener todas las deudas existentes
     * Las columnas son:
     *          0- id
     *          1- name
     *          2- amount
     */
    public abstract Reserves get();

    public abstract void add(final Reserve debt, final Controller.SQLcallback callback);

    public abstract void edit(final Reserve debt, final Controller.SQLcallback callback);

    public abstract void delete(final int id, final Controller.SQLcallback callback);

}
