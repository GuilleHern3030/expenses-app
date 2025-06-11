package enel.dev.budgets.views.shoppinglist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShoppingListDelete#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListDelete extends ShoppingListContext {

    private String listName;

    public ShoppingListDelete() {
        // Required empty public constructor
    }

    public static ShoppingListDelete newInstance(String listName) {
        ShoppingListDelete fragment = new ShoppingListDelete();
        Bundle args = new Bundle();
        args.putString("name", listName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {
            this.listName = getArguments().getString("name");
        } catch(Exception ignored) { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shoppinglist_delete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.bCancel).setOnClickListener(v -> closeFragment());
        view.findViewById(R.id.bAccept).setOnClickListener(v -> deleteList(listName));

        TextView listTextView = view.findViewById(R.id.list_name);
        listTextView.setText(listName);
    }

    private void deleteList(final String listName) {
        Controller.shoppingList(requireActivity()).remove(listName);
        restartFragment();
    }
}