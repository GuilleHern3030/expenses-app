package enel.dev.budgets.data.sql;

import android.content.Context;

import enel.dev.budgets.objects.shoppinglist.Item;
import enel.dev.budgets.objects.shoppinglist.ShoppingList;
import enel.dev.budgets.objects.shoppinglist.ShoppingListArray;

public abstract class ShoppingListSQL {

    protected static final String SHOPPING_LIST_TABLE = "SHOPPINGLIST";

    public final static String ARTICLE_SEPARATOR = "_";

    protected final Context context;
    protected final String DATA_BASE;
    public ShoppingListSQL(final Context context, final String dbName) {
        this.context = context;
        this.DATA_BASE = dbName;
    }

    static void createDefaultTables(BasicSQL sql) {
        sql.tablaCrear(SHOPPING_LIST_TABLE, new String[]{
                "listname", // data[0]
                "listcontent" // data[1]
        });
    }

    public abstract ShoppingList get();

    public abstract ShoppingList get(final String listName);

    public abstract ShoppingListArray getShoppingListArray();

    public abstract void add(final String shoppingListName, final Item item, final Controller.SQLcallback callback);

    public abstract void edit(final String shoppingListName, final String itemName, final Item item, final Controller.SQLcallback callback);

    public abstract void remove(final String shoppingListName, final String itemName, final Controller.SQLcallback callback);

    public abstract boolean add(final ShoppingList shoppingList);
    public abstract boolean remove(final String shoppingListName);

    public abstract boolean edit(final String listName, final ShoppingList shoppingList);

    public abstract void setFirst(final String listName);

    public abstract void setChecked(final String shoppingListName, final String itemName, final boolean checked);

}
