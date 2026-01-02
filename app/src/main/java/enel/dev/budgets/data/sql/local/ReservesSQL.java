package enel.dev.budgets.data.sql.local;

import android.content.Context;

import enel.dev.budgets.data.sql.BasicSQL;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.reserve.Reserve;
import enel.dev.budgets.objects.reserve.Reserves;

public class ReservesSQL extends enel.dev.budgets.data.sql.ReservesSQL {

    public ReservesSQL(final Context context, final String dbName) {
        super(context, dbName);
    }

    /**
     * Obtener todas las deudas existentes
     * Las columnas son:
     *          0- id
     *          1- name
     *          2- amount
     */
    @Override
    public Reserves get() {
        final Reserves reserves = new Reserves();
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        try {
            final int debtsSize = sql.tablaFilas(RESERVES_TABLE);
            for (int row = 0; row < debtsSize; row++) {
                String[] columns = sql.tablaObtenerFila(RESERVES_TABLE, row);
                reserves.add(new Reserve(
                        Integer.parseInt(columns[0]), // id
                        columns[1], // name
                        Double.parseDouble(columns[2]) // amount
                ));
            }
        } catch(Exception ignored) { }
        sql.cerrar();
        return reserves;
    }

    @Override
    public void add(final Reserve reserve, final Controller.SQLcallback callback) {
        if (reserve == null) { callback.onError("Invalid Reserve"); return; }
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final int success = sql.tablaIngresarFila(RESERVES_TABLE, new String[]{
                String.valueOf(reserve.id()), // id
                reserve.getName(), // name
                String.valueOf(reserve.getAmount()), // amount

        });
        sql.cerrar();
        if (success >= 0) callback.onSuccess();
        else callback.onError("");
    }

    @Override
    public void edit(final Reserve reserve, final Controller.SQLcallback callback) {
        boolean success = false;
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        if (reserve != null) try {
            final int id = reserve.id();
            final int row = sql.tablaBuscarFila(RESERVES_TABLE, "id", String.valueOf(id), false);
            if (row >= 0) {
                success = sql.tablaEditarFila(RESERVES_TABLE, row, new String[]{
                        String.valueOf(reserve.id()),
                        reserve.getName(),
                        String.valueOf(reserve.getAmount()),
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
            final int row = sql.tablaBuscarFila(RESERVES_TABLE, "id", String.valueOf(id), false);
            if (row >= 0)
                success = sql.tablaEliminarFila(RESERVES_TABLE, row, true);
        } catch (Exception ignored) { }
        sql.cerrar();
        if (success) callback.onSuccess();
        else callback.onError("");
    }

}
