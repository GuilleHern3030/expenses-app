package enel.dev.budgets.data.sql.local;

import android.content.Context;

import enel.dev.budgets.data.sql.BasicSQL;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.shoppinglist.Item;
import enel.dev.budgets.objects.shoppinglist.ShoppingList;
import enel.dev.budgets.objects.shoppinglist.ShoppingListArray;

public class ShoppingListSQL extends enel.dev.budgets.data.sql.ShoppingListSQL {

    public final static String ARTICLE_SEPARATOR = "_";

    public ShoppingListSQL(final Context context, final String dbName) {
        super(context, dbName);
    }

    public static void createDefaultTables(BasicSQL sql) {
        sql.tablaCrear(SHOPPING_LIST_TABLE, new String[]{
                "listname", // data[0]
                "listcontent" // data[1]
        });
    }

    private static ShoppingList parseShoppingList(final String[] data) {
        if (data != null && data.length > 0) {
            ShoppingList shoppingList = new ShoppingList(data[0]);
            String[] items = data[1].split(ARTICLE_SEPARATOR);
            for (String rawItem : items) try {
                final String itemDescription = rawItem.substring(0, rawItem.length() - 1);
                final boolean completed = Integer.parseInt(rawItem.substring(rawItem.length() - 1)) == 1;
                final Item item = new Item(itemDescription, completed);
                shoppingList.add(item);
            } catch (Exception ignored) { }
            return shoppingList;
        } else return null;
    }

    private static String[] parseShoppingList(final ShoppingList shoppingList) {
        return new String[] {
                shoppingList.getName(), // listname
                shoppingList.join() // listcontent
        };
    }

    @Override
    public ShoppingList get() {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        ShoppingList shoppingList = parseShoppingList(sql.tablaObtenerFila(SHOPPING_LIST_TABLE, 0));
        sql.cerrar();
        return shoppingList;
    }

    @Override
    public ShoppingList get(final String listName) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final int row = sql.tablaBuscarFila(SHOPPING_LIST_TABLE, "listname", listName, false);
        ShoppingList shoppingList = parseShoppingList(sql.tablaObtenerFila(SHOPPING_LIST_TABLE, row));
        sql.cerrar();
        return shoppingList;
    }

    @Override
    public ShoppingListArray getShoppingListArray() {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final ShoppingListArray lists = new ShoppingListArray();
        try {
            final int rows = sql.tablaFilas(SHOPPING_LIST_TABLE);
            for (int row = 0; row < rows; row++)
                lists.add(parseShoppingList(sql.tablaObtenerFila(SHOPPING_LIST_TABLE, row)));
        } catch(Exception ignored) { }
        sql.cerrar();
        return lists;
    }

    @Override
    public void add(final String shoppingListName, final Item item, final Controller.SQLcallback callback) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        add(sql, shoppingListName, item);
        sql.cerrar();
        callback.onSuccess();
    }

    public static void add(final BasicSQL sql, final String shoppingListName, final Item item) {
        final int row = sql.tablaBuscarFila(SHOPPING_LIST_TABLE, "listname", shoppingListName, false);
        final String[] data = sql.tablaObtenerFila(SHOPPING_LIST_TABLE, row);
        ShoppingList shoppingList = parseShoppingList(data);
        shoppingList.add(item);
        sql.tablaEditarFila(SHOPPING_LIST_TABLE, row, parseShoppingList(shoppingList));
    }

    @Override
    public void edit(final String shoppingListName, final String itemName, final Item item, final Controller.SQLcallback callback) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        edit(sql, shoppingListName, itemName, item);
        sql.cerrar();
        callback.onSuccess();
    }

    private void edit(final BasicSQL sql, final String shoppingListName, final String itemName, final Item item) {
        try {
            final int row = sql.tablaBuscarFila(SHOPPING_LIST_TABLE, "listname", shoppingListName, false);
            final String[] data = sql.tablaObtenerFila(SHOPPING_LIST_TABLE, row);
            final ShoppingList shoppingList = parseShoppingList(data);
            final int index = shoppingList.indexOf(itemName);
            shoppingList.set(index, item);
            sql.tablaEditarFila(SHOPPING_LIST_TABLE, row, parseShoppingList(shoppingList));
        } catch (Exception ignored) { }
    }

    @Override
    public void remove(final String shoppingListName, final String itemName, final Controller.SQLcallback callback) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        try {
            final int row = sql.tablaBuscarFila(SHOPPING_LIST_TABLE, "listname", shoppingListName, false);
            final String[] data = sql.tablaObtenerFila(SHOPPING_LIST_TABLE, row);
            final ShoppingList shoppingList = parseShoppingList(data);
            final int index = shoppingList.indexOf(itemName);
            shoppingList.remove(index);
            sql.tablaEditarFila(SHOPPING_LIST_TABLE, row, parseShoppingList(shoppingList));
        } catch (Exception ignored) { }
        sql.cerrar();
        callback.onSuccess();
    }

    @Override
    public boolean add(final ShoppingList shoppingList) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final int newRow = sql.tablaIngresarFila(SHOPPING_LIST_TABLE, parseShoppingList(shoppingList));
        sql.cerrar();
        return newRow != BasicSQL.ERROR;
    }

    @Override
    public boolean remove(final String shoppingListName) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final int row = sql.tablaBuscarFila(SHOPPING_LIST_TABLE, "listname", shoppingListName, false);
        final boolean success = sql.tablaEliminarFila(SHOPPING_LIST_TABLE, row, true);
        sql.cerrar();
        return success;
    }

    @Override
    public boolean edit(final String listName, final ShoppingList shoppingList) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final int row = sql.tablaBuscarFila(SHOPPING_LIST_TABLE, "listname", listName, false);
        final boolean success = sql.tablaEditarFila(SHOPPING_LIST_TABLE, row, parseShoppingList(shoppingList));
        sql.cerrar();
        return success;
    }

    @Override
    public void setFirst(final String listName) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final int rowId = sql.tablaBuscarFila(SHOPPING_LIST_TABLE, "listname", listName, false);
        if (rowId != 0) {
            final String[] row = sql.tablaObtenerFila(SHOPPING_LIST_TABLE, rowId);
            final String[] row0 = sql.tablaObtenerFila(SHOPPING_LIST_TABLE, 0);
            sql.tablaEditarFila(SHOPPING_LIST_TABLE, rowId, row0);
            sql.tablaEditarFila(SHOPPING_LIST_TABLE, 0, row);
        }
        sql.cerrar();
    }

    @Override
    public void setChecked(String shoppingListName, String itemName, boolean checked) {
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        edit(sql, shoppingListName, itemName, new Item(itemName, checked));
        sql.cerrar();
    }

}
