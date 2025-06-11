package enel.dev.budgets.views;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.money.Balance;
import enel.dev.budgets.objects.transaction.Transactions;
import enel.dev.budgets.views.budget.BudgetFragment;
import enel.dev.budgets.views.home.HomeFragment;
import enel.dev.budgets.views.shoppinglist.ShoppinglistFragment;
import enel.dev.budgets.views.summary.SummaryFragment;

public abstract class Fragment extends androidx.fragment.app.Fragment {

    protected Balance balance = new Balance();
    protected Transactions expenses = new Transactions();
    protected Transactions incomes = new Transactions();

    protected FrameLayout fragmentAbove;

    protected String[] params;

    public Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            requireView().findViewById(R.id.navigation_view_home).setOnClickListener(v -> goHome());
            requireView().findViewById(R.id.navigation_view_shoppinglist).setOnClickListener(v -> goShoppingList());
            requireView().findViewById(R.id.navigation_view_summary).setOnClickListener(v -> goSummary());
            requireView().findViewById(R.id.navigation_view_budgets).setOnClickListener(v -> goBudgets());
            requireView().findViewById(R.id.navigation_view_action).setOnClickListener(v -> onActionPressed());
        } catch (Exception ignored) { }

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        onBackPressed();
                    }
                });
    }

    protected static Bundle bundle(final String... params) {
        Bundle bundle = new Bundle();
        for (int i = 0; i < params.length; i++) bundle.putString("param"+i, params[i]);
        return bundle;
    }

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnChangeFragmentListener) {
            mListener = (OnChangeFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setOnChangeFragmentListener(OnChangeFragmentListener listener) {
        this.mListener = listener;
    }

    private OnChangeFragmentListener mListener;
    public interface OnChangeFragmentListener {
        void onChangeFragment(final Fragment newFragment);
    }

    protected void replaceFragment(final Fragment fragment) {
        if (mListener != null) mListener.onChangeFragment(fragment);
        getParentFragmentManager().beginTransaction().remove(this).commit(); // Remove this fragment
        //closeFragment();
    }

    protected void showFragment(final Fragment fragment) {
        replaceFragment(fragment);
    }

    protected final void goHome() {
        showFragment(new HomeFragment());
    }
    protected final void goSummary() {
        showFragment(new SummaryFragment());
    }
    protected final void goShoppingList() {
        showFragment(new ShoppinglistFragment());
    }
    protected final void goBudgets() {
        showFragment(new BudgetFragment());
    }

    protected void hideFragmentAbove() {
        FrameLayout fragmentAbove = requireView().findViewById(R.id.fragment_above);
        fragmentAbove.setVisibility(View.GONE);
        fragmentAbove.removeAllViews();
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        onBackPressed();
                    }
                });
    }
    //</editor-fold>

    public abstract void onActionPressed();

    public abstract void onBackPressed();
}
