package enel.dev.budgets.views.shoppinglist;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.shoppinglist.Item;
import enel.dev.budgets.utils.SnackBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShoppingListEditor#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListEditor extends ShoppingListContext {

    private Item item;
    private int id;
    private EditText etItem;

    public ShoppingListEditor() {
        // Required empty public constructor
    }

    public static ShoppingListEditor newInstance(String listName, Item item, int id) {
        ShoppingListEditor fragment = new ShoppingListEditor();
        Bundle args = new Bundle();
        args.putString("name", listName);
        args.putString("itemname", item.getName());
        args.putBoolean("ischecked", item.isCompleted());
        args.putInt("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    public static ShoppingListEditor newInstance(String listName, Item item) {
        return newInstance(listName, item, -1);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {
            id = getArguments().getInt("id");
            final String itemName = getArguments().getString("itemname");
            final boolean isChecked = getArguments().getBoolean("ischecked");
            item = new Item(itemName, isChecked);
        } catch(Exception ignored) { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shoppinglist_editor, container, false);

        // List title name
        TextView nameTextView = view.findViewById(R.id.budget_name);
        nameTextView.setText(listName);

        // Edit text
        etItem = view.findViewById(R.id.item_description);
        try {
            if (item.getName().length() > 0)
                etItem.setText(item.getName());
        } catch (Exception ignored) { }

        // Buttons
        view.findViewById(R.id.bCancel).setOnClickListener(v -> {
            hideKeyboard();
            closeFragment();
        });
        view.findViewById(R.id.bDelete).setOnClickListener(v -> {
            hideKeyboard();
            removeListItem();
        });
        view.findViewById(R.id.bAccept).setOnClickListener(v -> {
            hideKeyboard();
            editBudget(etItem.getText().toString());
        });

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try { // show keyboard
            etItem.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etItem, InputMethodManager.SHOW_IMPLICIT);
        } catch(Exception ignored) { }

    }

    private void removeListItem() {
        replaceFragment(ShoppingListRemover.newInstance(listName, item));
    }

    private void editBudget(final String description) {
        if (description != null && description.length() > 0) {
            final Item newItem = new Item(description, item.isCompleted());
            Controller.shoppingList(requireActivity()).edit(listName, item.getName(), newItem);
            editListItem(id, newItem);
        } else SnackBar.show(requireContext(), getView(), requireContext().getString(R.string.shopping_list_description_empty_error));
    }

    private void hideKeyboard() {
        try { // hide keyboard
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
        } catch (Exception ignored) { }
    }
}