package enel.dev.budgets.views.shoppinglist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.shoppinglist.Item;

public abstract class ShoppingListContext extends Fragment {

    protected String[] params;
    protected String listName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {
            this.listName = getArguments().getString("name");
            ArrayList<String> args = new ArrayList<>();
            int key = 0;
            while (getArguments().getString("param"+key) != null) {
                args.add(getArguments().getString("param"+key));
                key ++;
            }
            params = args.toArray(new String[0]);
        } catch(Exception ignored) { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        closeFragment();
                    }
                });

        return inflater.inflate(R.layout.fragment_budget_adder, container, false);
    }

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    private OnListChangeListener listener;
    public interface OnListChangeListener {
        void onListItemRemoved(final Item item);
        void onListItemAdded(final Item item);
        void onListItemEdited(final int id, final Item item);
        void onFragmentChanged(final ShoppingListContext frame);
        void onListReloadRequired();
        void onCancel();
    }

    public void setOnListChangeListener(OnListChangeListener listener) {
        this.listener = listener;
    }
    //</editor-fold>

    protected void closeFragment() {
        listener.onCancel();
    }

    protected void addListItem(final Item item) {
        listener.onListItemAdded(item);
    }

    protected void editListItem(final int id, final Item item) {
        listener.onListItemEdited(id, item);
    }

    protected void removeListItem(final Item item) {
        listener.onListItemRemoved(item);
    }

    protected void replaceFragment(final ShoppingListContext fragment) {
        listener.onFragmentChanged(fragment);
    }

    protected void restartFragment() {
        listener.onListReloadRequired();
    }


}
