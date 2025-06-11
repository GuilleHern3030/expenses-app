package enel.dev.budgets.data.sql;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

import enel.dev.budgets.objects.budget.Budget;
import enel.dev.budgets.objects.budget.Budgets;
import enel.dev.budgets.objects.category.Category;

/**
 * Controlador de las tablas de presupuesto
 * Cada tabla contiene las columnas:
 *      0- categoryname
 *      1- categoryimage
 *      2- categorycolor
 *      3- amount
 * Por otro lado, se encontrará la tabla que contiene el nombre de cada presupuesto
 */
public class BudgetController {

    private final static String DATA_BASE = "BUDGETS";
    private final static String TABLE_PARTIAL_NAME = "BUDGET";
    private final static String[] TABLE_COLUMNS = new String[]{"categoryname", "categoryimage", "categorycolor", "amount"};

    /**
     * Verificar si la Base de Datos existe
     * @param context Activity
     * @return True si la Base de Datos existe
     */
    public static boolean exists(final Context context) {
        final ArrayList<String> dbs = new ArrayList<>(Arrays.asList(BasicSQL.listarBasesDeDatos(context)));
        return dbs.contains(DATA_BASE);
    }

    /**
     * Sitúa el budget en la primera posición de la tabla de Budgets, así se posicionará como id 0
     * @param context Activity
     * @param budgetName Nombre del {@link Budgets} que se situará en primera posición como id 0
     */
    public static void setFirst(final Context context, final String budgetName) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final ArrayList<String> budgets = budgets(sql);
        final int index = budgets.indexOf(budgetName);
        if (index > 0) try {
            String[] row0 = sql.tablaObtenerFila(TABLE_PARTIAL_NAME, 0);
            String[] row = sql.tablaObtenerFila(TABLE_PARTIAL_NAME, index);
            sql.tablaEditarFila(TABLE_PARTIAL_NAME, 0, row);
            sql.tablaEditarFila(TABLE_PARTIAL_NAME, index, row0);
        } catch (Exception ignored) { }
        sql.cerrar();
    }

    /**
     * Obtener la cantidad total de {@link Budgets} que existen en la Base de Datos
     * @param context Activity context
     * @return {@link ArrayList<String>} con los nombres de los {@link Budgets}
     */
    public static ArrayList<String> budgets(final Context context) {
        ArrayList<String> budgets = new ArrayList<>();
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        if (!sql.tablaExiste(TABLE_PARTIAL_NAME))
            sql.tablaCrear(TABLE_PARTIAL_NAME, new String[]{"budgetname"});
        final int rows = sql.tablaFilas(TABLE_PARTIAL_NAME);
        for (int id = 0; id < rows; id++)
            budgets.add(sql.tablaObtenerFila(TABLE_PARTIAL_NAME, id)[0]);
        sql.cerrar();
        return budgets;
    }
    private static ArrayList<String> budgets(final BasicSQL sql) {
        ArrayList<String> budgets = new ArrayList<>();
        if (!sql.tablaExiste(TABLE_PARTIAL_NAME))
            sql.tablaCrear(TABLE_PARTIAL_NAME, new String[]{"budgetname"});
        final int rows = sql.tablaFilas(TABLE_PARTIAL_NAME);
        for (int id = 0; id < rows; id++)
            budgets.add(sql.tablaObtenerFila(TABLE_PARTIAL_NAME, id)[0]);
        return budgets;
    }

    /**
     * Obtiene el presupuesto guardado en el id específico
     * @param id id del presupuesto
     * @return Devuelve un {@link Budgets} con el id especificado o null si no existe
     */
    public static Budgets get(final Context context, final int id) {
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final ArrayList<String> budgetsStored = budgets(sql);
        Budgets result = id < budgetsStored.size() ? get(sql, id, budgetsStored.get(id)) : null;
        sql.cerrar();
        return result;
    }

    /**
     * Obtiene el presupuesto guardado en el id específico
     * @param budgetName nombre del presupuesto
     * @return Devuelve un {@link Budgets} con el budgetName o null si no se encuentra
     */
    public static Budgets get(final Context context, final String budgetName) {
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final ArrayList<String> budgetsStored = budgets(sql);
        final int id = budgetsStored.indexOf(budgetName);
        Budgets result = id != -1 ? get(sql, id, budgetName) : null;
        sql.cerrar();
        return result;
    }

    private static Budgets get(final BasicSQL sql, final int id, final String name) {
        Budgets budgets = new Budgets(id, name);
        /*if (sql.tablaExiste(tableName(name)))*/ try {
            final int rows = sql.tablaFilas(tableName(name));
            for (int row = 0; row < rows; row++) {
                final String[] data = sql.tablaObtenerFila(tableName(name), row);
                Budget budget = new Budget(
                        new Category(data[0], // categoryname
                                Integer.parseInt(data[1]), // categoryimage
                                Integer.parseInt(data[2])),// categorycolor
                        Double.parseDouble(data[3]) // amount
                );
                budgets.add(budget);
            }
        } catch (Exception ignored) { }
        return budgets;
    }

    public static Budgets get(final Context context) {
        return get(context, 0);
    }

    /**
     * Guarda la información de un {@link Budgets} en la Base de Datos.
     * Si el {@link Budgets} ya existe, se reescribe el existente.
     * @param context Activity
     * @param budgets Conjunto de {@link Budget}
     * @return Se obtiene True si la operación fue exitosa
     */
    public static boolean store(final Context context, final Budgets budgets) {
        if (budgets.getName().length() == 0) return false;
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final String tableName = tableName(budgets.getName());
        boolean result = false;
        try {
            if (budgets.getId() == -1) // budget not exists yet
                sql.tablaIngresarFila(TABLE_PARTIAL_NAME, new String[]{budgets.getName()});
            else // budget already exists
                sql.tablaEliminar(tableName);
            sql.tablaCrear(tableName, TABLE_COLUMNS);
            for (Budget budget : budgets) {
                sql.tablaIngresarFila(tableName, new String[] {
                        budget.getCategory().getName(), // categoryname
                        String.valueOf(budget.getCategory().getImageId()), // categoryimage
                        String.valueOf(budget.getCategory().getColorId()), // categorycolor
                        String.valueOf(budget.getAmount()) // amount
                });
            }
            result = true;
        } catch(Exception ignored) { }
        sql.cerrar();
        return result;
    }

    /**
     * Edita la información de un {@link Budgets} en la Base de Datos
     * @param context Activity
     * @param budgetName Nombre del {@link Budgets}
     * @param categoryName Nombre de la categoría que identificará el {@link Budget} a editar
     * @param amount Nuevo número de cantidad que será editado
     * @return true si la operacion fue exitosa
     */
    public static boolean edit(final Context context, final String budgetName, final String categoryName, final double amount) {
        if (budgetName == null || budgetName.length() == 0) return false;
        if (categoryName == null || categoryName.length() == 0) return false;
        boolean success = false;
        final String tableName = tableName(budgetName);
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        try {
            final int row = sql.tablaBuscarFila(tableName, "categoryname", categoryName, false);
            final String[] data = sql.tablaObtenerFila(tableName, row);
            if (row >= 0)
                success = sql.tablaEditarFila(tableName, row, new String[]{
                        data[0], // categoryname
                        data[1], // categoryimage
                        data[2], // categorycolor
                        String.valueOf(amount) // amount
                });
        } catch (Exception ignored) { }
        sql.cerrar();
        return success;
    }

    /**
     * Elimina una categoría de un {@link Budgets} en la Base de Datos
     * @param context Activity
     * @param budgetName Nombre del {@link Budgets}
     * @param categoryName Nombre de la categoría que identificará el {@link Budget} a eliminar
     * @return true si la operacion fue exitosa
     */
    public static boolean remove(final Context context, final String budgetName, final String categoryName) {
        if (budgetName == null || budgetName.length() == 0) return false;
        if (categoryName == null || categoryName.length() == 0) return false;
        boolean success = false;
        final String tableName = tableName(budgetName);
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        try {
            final int row = sql.tablaBuscarFila(tableName, "categoryname", categoryName, false);
            success = sql.tablaEliminarFila(tableName, row, true);
        } catch (Exception ignored) { }
        sql.cerrar();
        return success;
    }

    /**
     * Elimina un {@link Budgets} de la Base de Datos.
     * @param context Activity
     * @param budgetName Nombre del {@link Budgets}
     * @return True si la operación fue exitosa
     */
    public static boolean delete(final Context context, final String budgetName) {
        boolean result = false;
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final ArrayList<String> budgetsStored = budgets(sql);
        final String tableName = tableName(budgetName);
        final int id = budgetsStored.indexOf(budgetName);
        try { sql.tablaEliminar(tableName); } catch (Exception ignored) { }
        if (id != -1) try {
            final int row = sql.tablaBuscarFila(TABLE_PARTIAL_NAME, "budgetname", budgetName, false);
            if (row >= 0)
                result = sql.tablaEliminarFila(TABLE_PARTIAL_NAME, row, true);
        } catch (Exception ignored) { }
        sql.cerrar();
        return result;
    }

    /**
     * Elimina un {@link Budgets} de la Base de Datos.
     * @param context Activity
     * @param budgets {@link Budgets} que se desea eliminar
     * @return True si la operación fue exitosa
     */
    public static boolean delete(final Context context, final Budgets budgets) {
        if (budgets.getId() == -1) return false;
        return delete(context, budgets.getName());
    }

    /**
     * Agrega un {@link Budget} a un {@link Budgets} almacenado en una Base de Datos
     * @param context Activity
     * @param budgetName Nombre del {@link Budgets} que se desea editar
     * @param budget {@link Budget} que se desea agregar
     * @return Devuelve el {@link Budgets} actualizado
     */
    public static Budgets add(final Context context, final String budgetName, final Budget budget) {
        final Budgets budgets = get(context, budgetName);
        final int categoryId = budgets.indexOf(budget.getCategory().getName());
        if (categoryId != -1) {
            budgets.get(categoryId).add(budget.getAmount());
        } else budgets.add(budget);
        store(context, budgets);
        return budgets;
    }

    private static String tableName(final String budgetName) {
        return TABLE_PARTIAL_NAME + budgetName;
    }
}
