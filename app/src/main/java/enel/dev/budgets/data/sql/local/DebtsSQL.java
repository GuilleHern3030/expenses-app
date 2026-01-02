package enel.dev.budgets.data.sql.local;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.sql.BasicSQL;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.debt.Debt;
import enel.dev.budgets.objects.debt.Debts;
import enel.dev.budgets.objects.money.Balance;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.money.Money;

public class DebtsSQL extends enel.dev.budgets.data.sql.DebtsSQL {

    public DebtsSQL(final Context context, final String dbName) {
        super(context, dbName);
    }

    public static void createDefaultTables(@NonNull BasicSQL sql) {
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
    @Override
    public Debts get() {
        final Debts debts = new Debts();
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final Balance balance = BalancesSQL.get(sql);
        try {
            final int debtsSize = sql.tablaFilas(DEBTS_TABLE);
            for (int row = 0; row < debtsSize; row++) {
                String[] columns = sql.tablaObtenerFila(DEBTS_TABLE, row);
                debts.add(new Debt(
                        Integer.parseInt(columns[0]), // id
                        columns[1], // lendername
                        new Money(balance.getCoin(columns[2]), Double.parseDouble(columns[3])), // coinname, coinamount
                        columns[4] // desciption
                ));
            }
        } catch(Exception ignored) { }
        sql.cerrar();
        return debts;
    }

    @Override
    public void add(final Debt debt, final Controller.SQLcallback callback) {
        if (debt == null) { callback.onError("Invalid Debt"); return; }
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final int success = sql.tablaIngresarFila(DEBTS_TABLE, new String[]{
                String.valueOf(debt.id()), // id
                debt.getLender(), // lendername
                debt.getMoney().getCoin().getName(), // coinname
                String.valueOf(debt.getMoney().getAmount()), // coinamount
                debt.getDescription() // description
        });
        sql.cerrar();
        if (success >= 0) callback.onSuccess();
        else callback.onError("");
    }

    @Override
    public void edit(final Debt debt, final Controller.SQLcallback callback) {
        boolean success = false;
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        if (debt != null) try {
            final int id = debt.id();
            final int row = sql.tablaBuscarFila(DEBTS_TABLE, "id", String.valueOf(id), false);
            if (row >= 0) {
                success = sql.tablaEditarFila(DEBTS_TABLE, row, new String[]{
                        String.valueOf(debt.id()),
                        debt.getLender(),
                        debt.getMoney().getCoin().getName(),
                        String.valueOf(debt.getMoney().getAmount()),
                        debt.getDescription()
                });
            }
        } catch (Exception ignored) { }
        sql.cerrar();
        if (success) callback.onSuccess();
        else callback.onError("");
    }

    @Override
    public void delete(final int id, final Controller.SQLcallback callback) {
        boolean success = false;
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        if (id >= 0) try {
            final int row = sql.tablaBuscarFila(DEBTS_TABLE, "id", String.valueOf(id), false);
            if (row >= 0)
                success = sql.tablaEliminarFila(DEBTS_TABLE, row, true);
        } catch (Exception ignored) { }
        sql.cerrar();
        if (success) callback.onSuccess();
        else callback.onError("");
    }

    @Override
    public void deleteCoin(final Coin coin) {
        if (coin != null) try {
            final BasicSQL sql = new BasicSQL(context, DATA_BASE);
            final int[] rows = sql.tablaBuscarFilas(DEBTS_TABLE, "coinname", coin.getName(), false);
            for (int row : rows)
                sql.tablaEliminarFila(DEBTS_TABLE, row, true);
            sql.cerrar();
        } catch (Exception e) {
            Log.e(getClass().getName(), "deleteCoin: ", e);
        }
    }

    @Override
    public void editCoin(final Coin oldCoin, final Coin newCoin) {
        if (oldCoin != null && newCoin != null) try {
            final BasicSQL sql = new BasicSQL(context, DATA_BASE);
            final int[] rows = sql.tablaBuscarFilas(DEBTS_TABLE, "coinname", oldCoin.getName(), false);
            for (int row : rows) {
                final String[] data = sql.tablaObtenerFila(DEBTS_TABLE, row);
                sql.tablaEditarFila(DEBTS_TABLE, row, new String[]{
                        data[0], // id
                        data[1], // lender
                        newCoin.getName(), // coinname
                        data[3], // amount
                        data[4], // description
                });
            }
            sql.cerrar();
        } catch (Exception e) {
            Log.e(getClass().getName(), "deleteCoin: ", e);
        }
    }

}
