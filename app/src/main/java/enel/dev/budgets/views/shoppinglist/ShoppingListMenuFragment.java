package enel.dev.budgets.views.shoppinglist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.shoppinglist.ShoppingListArray;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShoppingListMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListMenuFragment extends ShoppingListContext {

    private final ArrayList<String> lists = new ArrayList<>();
    private ShoppingListMenuListLayout listLayout;

    public ShoppingListMenuFragment() {
        // Required empty public constructor
    }

    public static ShoppingListMenuFragment newInstance(final ShoppingListArray lists) {
        ShoppingListMenuFragment fragment = new ShoppingListMenuFragment();
        Bundle args = new Bundle();
        if (lists != null && lists.size() > 0)
            for (int i = 0; i < lists.size(); i++)
                args.putString("param" + i, lists.get(i).getName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            lists.addAll(Arrays.asList(params));
        } catch (Exception ignored) { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Registra un callback para el botón "Atrás"
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        closeFragment();
                    }
                });

        return inflater.inflate(R.layout.fragment_list_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.background).setOnClickListener(v -> closeFragment());
        view.findViewById(R.id.scrollview).setOnClickListener(v -> closeFragment());
        view.findViewById(R.id.bAdd).setOnClickListener(v -> replaceFragment(ShoppingListCreator.newInstance(lists)));
        listLayout = new ShoppingListMenuListLayout(requireContext(), view.findViewById(R.id.list_view));
        listLayout.showContent(lists);
        listLayout.setOnBudgetCLickListener(new ShoppingListMenuListLayout.OnElementClickListener() {
            @Override
            public void listClicked(String listName) {
                Controller.shoppingList(requireActivity()).setFirst(listName);
                restartFragment();
            }

            @Override
            public boolean listLongClicked(String listName) {
                replaceFragment(ShoppingListDelete.newInstance(listName));
                return true;
            }
        });
    }
}