package enel.dev.budgets.views.categorysummary;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;
import enel.dev.budgets.objects.transaction.TransactionsArray;
import enel.dev.budgets.views.date.DateFragment;
import enel.dev.budgets.views.editor.transaction.DeleteLayout;
import enel.dev.budgets.views.editor.transaction.TransactionFragment;
import enel.dev.budgets.views.editor.transaction.TransactionImage;
import enel.dev.budgets.views.summary.TransactionsByDayListLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategorySummary#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategorySummary extends Fragment {

    private FrameLayout secondaryFragment;

    public CategorySummary() {
        // Required empty public constructor
    }

    private String category;
    private Date date1;
    private Date date2;

    private View view;

    private TransactionsByDayListLayout transactionsListLayout;

    private boolean changesRealized = false;


    //<editor-fold defaultstate="collapsed" desc=" Constructor ">
    public static CategorySummary newInstance(final String category, final Date date1, final Date date2) {
        CategorySummary fragment = new CategorySummary();
        Bundle args = new Bundle();
        args.putString("category", category);
        args.putString("date1", date1.toString());
        args.putString("date2", date2.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public static CategorySummary newInstance(final String category, final Date date) {
        CategorySummary fragment = new CategorySummary();
        Bundle args = new Bundle();
        args.putString("category", category);
        args.putString("date1", date.toString());
        args.putString("date2", date.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {
            this.category = getArguments().getString("category");
            this.date1 = new Date(getArguments().getString("date1"));
            this.date2 = new Date(getArguments().getString("date2"));
        } catch(Exception ignored) { }
    }
    //</editor-fold>

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_summary_categories, container, false);

        // Registra un callback para el botón "Atrás"
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        closeFragment();
                    }
                });

        this.transactionsListLayout = new TransactionsByDayListLayout(
                requireActivity(),
                view.findViewById(R.id.list_view),
                Preferences.decimalFormat(requireActivity())
        );

        return this.view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.secondaryFragment = view.findViewById(R.id.secondary_fragment);

        view.findViewById(R.id.bBack).setOnClickListener(v -> closeFragment());

        TextView actionBarDate = view.findViewById(R.id.summary_date);

        if (date1.encode() == date2.encode()) actionBarDate.setText(date1.getMonthNameAndYear(requireActivity()));
        else actionBarDate.setText(date1.formatedDateExtended(requireActivity()) + " -> " + date2.formatedDateExtended(requireActivity()));

        actualizeTransactionsList();
    }

    private void actualizeTransactionsList() {

        Transactions transactions = date1.partialEncode() == date2.partialEncode() ?
                Controller.transactions(requireActivity()).get(date1.getYear(), date1.getMonth()).filterCategory(category).filterCoin(Preferences.defaultCoin(requireActivity()).getName()) :
                Controller.transactions(requireActivity()).get(date1, date2).filterCategory(category).filterCoin(Preferences.defaultCoin(requireActivity()).getName());

        TransactionsArray transactionsArray = new TransactionsArray(transactions);

        if (transactionsArray.size() == 0) closeFragment();

        // ListView
        transactionsListLayout.showContent(transactionsArray);
        transactionsListLayout.setOnTransactionCLick(new TransactionsByDayListLayout.OnDayClickListener() {
            @Override
            public void transactionClicked(int i, int row, Transaction transaction) {
                showEditorFragment(i, row, transaction);
            }

            @Override
            public void transactionPhotoClicked(int i, int row, Transaction transaction) {
                TransactionImage transactionImage = TransactionImage.newInstance(transaction);
                transactionImage.setOnFragmentInteractionListener(() -> hideSecondaryFragment());
            }

            @Override
            public void transactionLongClicked(int i, int row, Transaction transaction) {
                showDeleterFragment(i, row, transaction);
            }

            @Override
            public void transactionDayClicked(Date date) {
                showDateFragment(date);
            }
        });

        TextView transactionsAmount = view.findViewById(R.id.transaction_total_amount);
        TextView transactionsQuantity = view.findViewById(R.id.transaction_quantity);

        transactionsAmount.setText(transactions.total(Preferences.defaultCoin(requireActivity())).toString(Preferences.decimalFormat(requireActivity())));
        transactionsQuantity.setText(String.valueOf(transactions.length()));
    }

    public void showEditorFragment(final int i, final int row, final Transaction transaction) {
        TransactionFragment secondaryFragment = TransactionFragment.newInstance(transaction, row);
        secondaryFragment.setOnTransactionChangeListener(new TransactionFragment.OnTransactionChangeListener() {
            @Override
            public void onTransactionCreate(final Transaction transaction) {
                changesRealized = true;
                hideSecondaryFragment();
                actualizeTransactionsList();
            }

            @Override
            public void onTransactionDelete(final Transaction transaction) {
                changesRealized = true;
                hideSecondaryFragment();
                actualizeTransactionsList();
            }

            @Override
            public void onTransactionEdited(final Transaction oldTransaction, final Transaction newTransaction) {
                changesRealized = true;
                hideSecondaryFragment();
                actualizeTransactionsList();
            }

            @Override
            public void onCancelTransactionOperation() {
                hideSecondaryFragment();
            }
        });
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.secondary_fragment, secondaryFragment);
        fragmentTransaction.commit();
        this.secondaryFragment.setVisibility(View.VISIBLE);
    }

    private void showDeleterFragment(final int i, final int row, final Transaction transaction) {
        DeleteLayout fragment = DeleteLayout.newInstance(transaction);
        fragment.setOnDeleteListener(new DeleteLayout.OnDeleteListener() {
            @Override
            public void onSuccessDelete() {
                changesRealized = true;
                hideSecondaryFragment();
                actualizeTransactionsList();
            }

            @Override
            public void onCancelDelete() {
                hideSecondaryFragment();
            }
        });
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.secondary_fragment, fragment);
        fragmentTransaction.commit();
        this.secondaryFragment.setVisibility(View.VISIBLE);
    }

    private void showDateFragment(final Date date) {
        DateFragment fragment = DateFragment.newInstance(date);
        fragment.setOnChangeFragmentListener(this::showFragment);
        fragment.setOnCloseFragmentListener(changes -> {
            if (changes)
                actualizeTransactionsList();
            hideSecondaryFragment();
        });
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.secondary_fragment, fragment);
        fragmentTransaction.commit();
        this.secondaryFragment.setVisibility(View.VISIBLE);
    }

    private void hideSecondaryFragment() {
        this.secondaryFragment.setVisibility(View.GONE);
    }


    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        kListener = null;
    }

    public void setOnFragmentCategoryListener(OnFragmentCategoryListener listener) {
        this.mListener = listener;
    }

    public void setOnChangeFragmentListener(OnChangeFragmentListener listener) {
        this.kListener = listener;
    }

    private OnFragmentCategoryListener mListener;
    private OnChangeFragmentListener kListener;
    public interface OnFragmentCategoryListener {
        void onFragmentClose(final boolean changesRealized);
    }

    public interface OnChangeFragmentListener {
        void onChangeFragment(final enel.dev.budgets.views.Fragment fragment);
    }

    protected void closeFragment() {
        if (mListener != null) mListener.onFragmentClose(changesRealized);
        getParentFragmentManager().beginTransaction().remove(this).commit(); // Remove this fragment
    }

    private void showFragment(enel.dev.budgets.views.Fragment fragment) {
        kListener.onChangeFragment(fragment);
    }
    //</editor-fold>
}