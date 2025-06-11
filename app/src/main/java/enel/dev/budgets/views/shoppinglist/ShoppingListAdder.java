package enel.dev.budgets.views.shoppinglist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.shoppinglist.Item;
import enel.dev.budgets.utils.SnackBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShoppingListAdder#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListAdder extends ShoppingListContext {

    public ShoppingListAdder() {
        // Required empty public constructor
    }

    public static ShoppingListAdder newInstance(final String listName) {
        ShoppingListAdder fragment = new ShoppingListAdder();
        Bundle args = new Bundle();
        args.putString("name", listName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shoppinglist_adder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvListName = view.findViewById(R.id.list_name);
        tvListName.setText(listName);

        EditText etItemName = view.findViewById(R.id.item_name);

        view.findViewById(R.id.bAccept).setOnClickListener(v -> addItem(etItemName.getText().toString()));
        view.findViewById(R.id.bCancel).setOnClickListener(v -> closeFragment());
    }

    private void addItem(final String name) {
        if (name != null && name.length() > 0) {
            final Item item = new Item(name);
            Controller.shoppingList(requireActivity()).add(listName, item);
            addListItem(item);
        } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.shopping_list_description_empty_error));
    }
}