package enel.dev.budgets.objects.shoppinglist;

import java.util.ArrayList;

public class ShoppingListArray extends ArrayList<ShoppingList> {

    public int indexOf(final String listName) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getName().equals(listName))
                return i;
        }
        return -1;
    }

    public ShoppingList get(String listName) {
        final int index = indexOf(listName);
        return (index >= 0) ? this.get(index) : null;
    }

    public ArrayList<String> getListsName() {
        ArrayList<String> lists = new ArrayList<>();
        for (ShoppingList list : this)
            lists.add(list.getName());
        return lists;
    }

}
