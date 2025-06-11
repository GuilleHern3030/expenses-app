package enel.dev.budgets.views.date;

import android.os.Bundle;

import enel.dev.budgets.data.livedata.LinearLayoutViewModel;
import enel.dev.budgets.data.livedata.TransactionsViewModel;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import enel.dev.budgets.R;
import enel.dev.budgets.views.budget.BudgetFragment;
import enel.dev.budgets.views.editor.transaction.TransactionFragment;
import enel.dev.budgets.views.editor.transaction.TransactionSelectorFragment;
import enel.dev.budgets.views.editor.transaction.TransactionImage;
import enel.dev.budgets.views.home.HomeFragment;
import enel.dev.budgets.views.home.TransactionsListLayout;
import enel.dev.budgets.views.shoppinglist.ShoppinglistFragment;
import enel.dev.budgets.views.summary.SummaryFragment;

public class DateFragment extends Fragment {

    //<editor-fold defaultstate="collapsed" desc=" Constructor ">
    private Date date = null;
    private TransactionsListLayout transactionsList;
    private TextView transactionsEmpty;
    private TextView transactionsBalance;
    private FrameLayout transactionsBalanceContainer;
    private FrameLayout fragmentAbove;
    private boolean changes = false;

    public DateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment.
     */
    public static DateFragment newInstance(final Date dateEncoded) {
        DateFragment fragment = new DateFragment();
        Bundle bundle = new Bundle();
        bundle.putString("date", dateEncoded.toString());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {
                final String dateEncoded = getArguments().getString("date");
                this.date = dateEncoded != null ? new Date(getArguments().getString("date")) : new Date();
        } catch(Exception ignored) { }
        if (this.date == null)
            this.date = new Date();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_date, container, false);

        fragmentAbove = view.findViewById(R.id.fragment_above);

        transactionsList = new TransactionsListLayout(getContext(), (LinearLayout)view.findViewById(R.id.date_list), Preferences.decimalFormat(requireActivity()));
        transactionsEmpty = view.findViewById(R.id.date_list_empty);
        transactionsBalance = view.findViewById(R.id.transactions_day_balance);
        transactionsBalanceContainer = view.findViewById(R.id.transactions_day_balance_frame);

        final TextView dateInView = view.findViewById(R.id.date);

        loadData(this.date);

        dateInView.setText(this.date.formatedDateExtended(requireActivity()));

        view.findViewById(R.id.bYesterday).setOnClickListener(v -> {
            this.date = this.date.yesterday();
            dateInView.setText(this.date.formatedDateExtended(requireActivity()));
            loadData(this.date);
        });

        view.findViewById(R.id.bTomorrow).setOnClickListener(v -> {
            this.date = this.date.tomorrow();
            dateInView.setText(this.date.formatedDateExtended(requireActivity()));
            loadData(this.date);
        });

        view.findViewById(R.id.navigation_view_action).setOnClickListener(v -> onActionPressed());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        close();
                    }
                });

        view.findViewById(R.id.navigation_view_home).setOnClickListener(v -> goFragment(new HomeFragment()));
        view.findViewById(R.id.navigation_view_shoppinglist).setOnClickListener(v -> goFragment(new ShoppinglistFragment()));
        view.findViewById(R.id.navigation_view_summary).setOnClickListener(v -> goFragment(new SummaryFragment()));
        view.findViewById(R.id.navigation_view_budgets).setOnClickListener(v -> goFragment(new BudgetFragment()));

        transactionsList.setOnTransactionCLick(new TransactionsListLayout.OnElementClickListener() {
            @Override
            public void transactionClicked(int index, Transaction transaction) {
                showTransactionFragment(TransactionFragment.newInstance(transaction, index));
            }

            @Override
            public boolean transactionLongClicked(int index, Transaction transaction) {
                showTransactionFragment(TransactionFragment.newInstance(transaction, true, index));
                return true;
            }

            @Override
            public void transactionPhoto(int row, Transaction transaction) {
                TransactionImage transactionImage = TransactionImage.newInstance(transaction);
                transactionImage.setOnFragmentInteractionListener(() -> hideFragmentAbove());
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_above, transactionImage);
                fragmentTransaction.commit();
                fragmentAbove.setVisibility(View.VISIBLE);
            }
        });
    }
    //</editor-fold>

    public void onActionPressed() {
        TransactionSelectorFragment transactionFragment = TransactionSelectorFragment.newInstance(this.date, false);
        transactionFragment.setOnTransactionChangeListener(new TransactionSelectorFragment.OnTransactionListener() {
            @Override
            public void onTransactionOperationRequired(TransactionFragment transactionFragment) {
                showTransactionFragment(transactionFragment);
            }

            @Override
            public void onDebtOperationRequired() {
                hideFragmentAbove();
            }

            @Override
            public void onCancelOperation() {
                hideFragmentAbove();
            }
        });
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, transactionFragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    /** Actualiza las vistas con las transacciones que reciba como parámetro **/
    public void showListView(final Transactions transactions) {
        if (transactions.length() > 0) {
            transactionsList.setVisibility(View.VISIBLE);
            transactionsEmpty.setVisibility(View.GONE);
            transactionsBalanceContainer.setVisibility(View.VISIBLE);
            transactionsBalance.setText(transactions.balance().total(Preferences.defaultCoin(requireActivity())).toString(Preferences.decimalFormat(requireActivity())));

            final LinearLayoutViewModel inflaterViewModel = new ViewModelProvider(this).get(LinearLayoutViewModel.class);
            inflaterViewModel.isInflating().observe(getViewLifecycleOwner(), isInflating -> { if (isInflating) transactionsList.showLoading(); });
            inflaterViewModel.getLayout().observe(getViewLifecycleOwner(), layout -> transactionsList.showContent(layout));
            inflaterViewModel.showTransactions(requireContext(), transactionsList, transactions.filterCoin(Preferences.defaultCoin(requireActivity()).getName()));

            //transactionsList.showContent(transactions.filterCoin(Preferences.defaultCoin(requireActivity()).getName()));

        } else {
            transactionsList.setVisibility(View.GONE);
            transactionsBalanceContainer.setVisibility(View.GONE);
            transactionsEmpty.setVisibility(View.VISIBLE);
        }
    }

    /** Carga las transacciones de la fecha seleccionada y, tras finalizar, actualiza las vistas **/
    private void loadData(final Date date) {

        TransactionsViewModel transaccionViewModel = new TransactionsViewModel();
        transaccionViewModel.isDataLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading)
                transactionsList.showLoading();
        });

        transaccionViewModel.getTransactions().observe(getViewLifecycleOwner(), transactionsLoaded -> {
            Transactions transactions = new Transactions();
            transactions.addAll(transactionsLoaded.incomes().today(date));
            transactions.addAll(transactionsLoaded.expenses().today(date));
            transaccionViewModel.removeData();
            showListView(transactions);
        });

        transaccionViewModel.loadTransactions(requireActivity(), date);
    }

    /** Muestra el editor/creador de transacciones **/
    private void showTransactionFragment(TransactionFragment fragment) {
        fragment.setOnTransactionChangeListener(new TransactionFragment.OnTransactionChangeListener() {
            @Override
            public void onTransactionCreate(Transaction transaction) {
                hideFragmentAbove();
                loadData(date);
                changes = true;
            }

            @Override
            public void onTransactionDelete(Transaction transaction) {
                hideFragmentAbove();
                loadData(date);
                changes = true;
            }

            @Override
            public void onTransactionEdited(final Transaction oldTransaction, final Transaction newTransaction) {
                hideFragmentAbove();
                loadData(date);
                changes = true;
            }

            @Override
            public void onCancelTransactionOperation() {
                hideFragmentAbove();
            }
        });
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    private void hideFragmentAbove() {
        FrameLayout fragmentAbove = requireView().findViewById(R.id.fragment_above);
        fragmentAbove.setVisibility(View.GONE);
        fragmentAbove.removeAllViews();
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        close();
                    }
                });
    }

    public void setOnCloseFragmentListener(OnCloseFragmentListener listener) {
        this.mListener = listener;
    }

    public void setOnChangeFragmentListener(OnChangeFragmentListener listener) {
        this.kListener = listener;
    }

    private OnCloseFragmentListener mListener;
    private OnChangeFragmentListener kListener;
    public interface OnCloseFragmentListener {
        void onCloseFragment(final boolean changes);
    }

    public interface OnChangeFragmentListener {
        void onChangeFragment(final enel.dev.budgets.views.Fragment fragment);
    }

    private void close() {
        mListener.onCloseFragment(changes);
    }

    private void goFragment(enel.dev.budgets.views.Fragment fragment) {
        kListener.onChangeFragment(fragment);
    }


}