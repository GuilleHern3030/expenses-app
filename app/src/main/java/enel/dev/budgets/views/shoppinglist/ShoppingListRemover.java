package enel.dev.budgets.views.shoppinglist;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.shoppinglist.Item;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShoppingListRemover#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListRemover extends ShoppingListContext {

    private String itemName;

    public ShoppingListRemover() {
        // Required empty public constructor
    }

    public static ShoppingListRemover newInstance(String listName, Item item) {
        ShoppingListRemover fragment = new ShoppingListRemover();
        Bundle args = new Bundle();
        args.putString("name", listName);
        args.putString("itemname", item.getName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {
            this.itemName = getArguments().getString("itemname");
        } catch(Exception ignored) { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shoppinglist_remover, container, false);

        TextView tvItemName = view.findViewById(R.id.item_name);
        tvItemName.setText(itemName);

        view.findViewById(R.id.bCancel).setOnClickListener(v -> closeFragment());
        view.findViewById(R.id.bAccept).setOnClickListener(v -> removeList(listName, itemName));

        return view;
    }

    private void removeList(final String listName, final String itemName) {
        Controller.shoppingList(requireActivity()).remove(listName, itemName);
        removeListItem(new Item(itemName));
    }
}