package enel.dev.budgets.views.shoppinglist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.shoppinglist.ShoppingList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShoppingListCreator#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListCreator extends ShoppingListContext {

    private final static int MAX_CHARACTERS = 24;
    private EditText etName;
    private final ArrayList<String> listStored = new ArrayList<>();

    public ShoppingListCreator() {
        // Required empty public constructor
    }

    public static ShoppingListCreator newInstance(ArrayList<String> lists) {
        ShoppingListCreator fragment = new ShoppingListCreator();
        Bundle args = new Bundle();
        if (lists != null && lists.size() > 0)
            for (int i = 0; i < lists.size(); i++)
                args.putString("param" + i, lists.get(i));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            listStored.addAll(Arrays.asList(params));
        } catch (Exception ignored) { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shoppinglist_creator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.etBudgetName);
        view.findViewById(R.id.bAccept).setOnClickListener(v -> createList());
        view.findViewById(R.id.bCancel).setOnClickListener(v -> closeFragment());

    }

    private void createList() {
        String name = etName.getText().toString();
        if (name.length() > 0) {
            if (name.length() > MAX_CHARACTERS) name = name.substring(0, MAX_CHARACTERS);
            if (!listStored.contains(name)) {
                Controller.shoppingList(requireContext()).add(new ShoppingList(name));
                Controller.shoppingList(requireContext()).setFirst(name);
            }
            restartFragment();
        } else closeFragment();
    }
}