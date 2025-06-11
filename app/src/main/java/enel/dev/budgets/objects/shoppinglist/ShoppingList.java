package enel.dev.budgets.objects.shoppinglist;

import java.util.ArrayList;

import enel.dev.budgets.data.sql.ShoppingListSQL;

public class ShoppingList extends ArrayList<Item> {

    private final String listName;

    public ShoppingList(final String listName) {
        this.listName = listName;
    }

    public String getName() {
        return this.listName;
    }

    public String join() {
        StringBuilder join = new StringBuilder();
        for (int i = 0; i < this.size(); i++) {
            join.append(this.get(i).join());
            if (i + 1 < this.size())
                join.append(ShoppingListSQL.ARTICLE_SEPARATOR);
        }
        return join.toString();
    }

    public int indexOf(final String itemName) {
        for (int i = 0; i < this.size(); i++)
            if (this.get(i).getName().equals(itemName))
                return i;
        return -1;
    }
}
