package enel.dev.budgets.views.shoppinglist;

import android.os.Bundle;

import enel.dev.budgets.data.livedata.ShoppingListsViewModel;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.shoppinglist.Item;
import enel.dev.budgets.objects.shoppinglist.ShoppingList;
import enel.dev.budgets.objects.shoppinglist.ShoppingListArray;
import enel.dev.budgets.views.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


import enel.dev.budgets.R;

public class ShoppinglistFragment extends Fragment implements ShoppingListContext.OnListChangeListener {

    private ShoppingListListLayout shoppingListListLayout;
    private TextView shoppingListTitleTextView;
    private ImageView abMenu;
    private ImageView bDelete;
    private ShoppingListArray shoppingLists;
    private ShoppingList currentList;
    private FrameLayout actionBar;

    public ShoppinglistFragment() {
        // Required empty public constructor
    }

    public static ShoppinglistFragment newInstance(String... params) {
        ShoppinglistFragment fragment = new ShoppinglistFragment();
        fragment.setArguments(bundle(params));
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActionPressed() {
        if (currentList != null)
            showFragmentAbove(ShoppingListAdder.newInstance(currentList.getName()));
        else showFragmentAbove(ShoppingListCreator.newInstance(shoppingLists.getListsName()));
    }

    @Override
    public void onBackPressed() {
        goHome();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shoppinglist, container, false);

        // Action bar
        this.actionBar = view.findViewById(R.id.action_bar);
        this.abMenu = view.findViewById(R.id.menu);
        abMenu.setOnClickListener(v -> showFragmentAbove(ShoppingListMenuFragment.newInstance(shoppingLists)));

        fragmentAbove = view.findViewById(R.id.fragment_above);

        // ShoppingList view
        shoppingListListLayout = new ShoppingListListLayout(requireActivity(), view.findViewById(R.id.list_view));
        shoppingListTitleTextView = view.findViewById(R.id.list_title);
        bDelete = view.findViewById(R.id.bDelete);
        bDelete.setOnClickListener(v -> showFragmentAbove(ShoppingListDelete.newInstance(currentList.getName())));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadShoppingLists();
        view.findViewById(R.id.navigation_view_shoppinglist).setOnClickListener(v -> {});
    }

    private void loadShoppingLists() {

        ShoppingListsViewModel viewModel = new ViewModelProvider(this).get(ShoppingListsViewModel.class);

        // Observa el estado de carga
        viewModel.isDataLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                shoppingListListLayout.showLoading();
                abMenu.setOnClickListener(v -> { });
                bDelete.setVisibility(View.GONE);
            } else {
                abMenu.setOnClickListener(v -> showFragmentAbove(ShoppingListMenuFragment.newInstance(shoppingLists)));
                if (shoppingLists.size() > 0)
                    bDelete.setVisibility(View.VISIBLE);
            }
        });

        // Observa los datos cargados
        viewModel.getLists().observe(getViewLifecycleOwner(), array -> {
            shoppingLists = array;
            if (array == null || array.size() == 0)
                showList(null);
            else showList(array.get(0));
        });

        // Cargar los datos
        viewModel.loadShoppingLists(requireActivity());
    }

    private void showList(final ShoppingList list) {
        this.currentList = list;

        if (list != null) {
            bDelete.setVisibility(View.VISIBLE);
            shoppingListTitleTextView.setText(list.getName());
            actionBar.setBackgroundResource(R.color.cardview_title_background);
            shoppingListTitleTextView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.cardview_title_foreground));
        } else {
            shoppingListTitleTextView.setText(requireActivity().getString(R.string.shoppinglist));
            actionBar.setBackgroundResource(R.color.action_bar_background);
            shoppingListTitleTextView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.action_bar_foreground));
            bDelete.setVisibility(View.GONE);
        }

        shoppingListListLayout.showContent(list);
        shoppingListListLayout.setOnItemCLickListener(new ShoppingListListLayout.OnItemCLickListener() {
            @Override
            public void itemClicked(int i, Item listItem) {
                showFragmentAbove(ShoppingListEditor.newInstance(currentList.getName(), listItem, i));
            }

            @Override
            public void itemCheckClicked(int i, Item listItem, boolean isChecked) {
                listItem.setCompleted(isChecked);
                Controller.shoppingList(requireActivity()).edit(currentList.getName(), listItem.getName(), listItem);
            }

            @Override
            public boolean itemLongClicked(int i, Item listItem) {
                showFragmentAbove(ShoppingListRemover.newInstance(currentList.getName(), listItem));
                return true;
            }
        });
    }

    private void showFragmentAbove(ShoppingListContext fragment) {
        fragment.setOnListChangeListener(this);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    @Override
    public void onListItemRemoved(Item item) {
        try {
            hideFragmentAbove();
            final int index = this.currentList.indexOf(item.getName());
            this.currentList.remove(index);
            showList(this.currentList);
        } catch(Exception ignored) {
            loadShoppingLists();
        }
    }

    @Override
    public void onListItemAdded(Item item) {
        try {
            hideFragmentAbove();
            this.currentList.add(item);
            showList(this.currentList);
        } catch(Exception ignored) {
            loadShoppingLists();
        }
    }

    @Override
    public void onListItemEdited(final int id, Item item) {
        try {
            hideFragmentAbove();
            this.currentList.set(id, item);
            showList(this.currentList);
        } catch(Exception e) {
            Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_SHORT).show();
            loadShoppingLists();
        }
    }

    @Override
    public void onFragmentChanged(ShoppingListContext frame) {
        showFragmentAbove(frame);
    }

    @Override
    public void onListReloadRequired() {
        hideFragmentAbove();
        loadShoppingLists();
    }

    @Override
    public void onCancel() {
        hideFragmentAbove();
    }
}