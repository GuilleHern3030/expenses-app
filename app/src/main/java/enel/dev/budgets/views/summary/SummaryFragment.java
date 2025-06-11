package enel.dev.budgets.views.summary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import enel.dev.budgets.R;
import enel.dev.budgets.data.livedata.LinearLayoutViewModel;
import enel.dev.budgets.data.livedata.TransactionsViewModel;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;
import enel.dev.budgets.views.Fragment;
import enel.dev.budgets.views.categorysummary.CategorySummary;
import enel.dev.budgets.views.date.DateFragment;
import enel.dev.budgets.views.editor.transaction.TransactionFragment;
import enel.dev.budgets.views.editor.transaction.TransactionImage;
import enel.dev.budgets.views.pdf.PdfFragment;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class SummaryFragment extends Fragment implements TransactionFragment.OnTransactionChangeListener, SummaryListLayout.onCategoryClickListener, TransactionsByDayListLayout.OnDayClickListener {

    private Date date = new Date();
    private Date date2 = new Date();
    private boolean expenses = true;
    private boolean incomes = false;

    //<editor-fold defaultstate="collapsed" desc=" Constructor ">
    private TransactionsByDayListLayout transactionsList;
    private SummaryListLayout summaryExpenses;
    private SummaryListLayout summaryIncomes;
    private SummaryView summaryBalance;


    public static SummaryFragment newInstance(final Category category) {
        Bundle args = new Bundle();
        args.putString("category", category.getName());
        SummaryFragment fragment = new SummaryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SummaryFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {
            final String category = getArguments().getString("category");
            showCategoryFragment(CategorySummary.newInstance(category, new Date()));
        } catch(Exception ignored) { }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final NumberFormat decimalFormat = Preferences.decimalFormat(requireActivity());
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        final LinearLayout bShowBalance = view.findViewById(R.id.bShowBalance);
        final LinearLayout bShowIncomes = view.findViewById(R.id.bShowIncomes);
        final LinearLayout bShowExpenses = view.findViewById(R.id.bShowExpenses);

        bShowExpenses.setOnClickListener(v -> showExpenses(bShowIncomes, bShowExpenses, bShowBalance));
        bShowIncomes.setOnClickListener(v -> showIncomes(bShowIncomes, bShowExpenses, bShowBalance));
        bShowBalance.setOnClickListener(v -> showBalance(bShowIncomes, bShowExpenses, bShowBalance));

        fragmentAbove = view.findViewById(R.id.fragment_above);

        transactionsList = new TransactionsByDayListLayout(requireActivity(), view.findViewById(R.id.transactions_list), Preferences.decimalFormat(requireActivity()));
        transactionsList.setOnTransactionCLick(this);

        view.findViewById(R.id.floatingButton).setOnClickListener(v -> showPdfFragment(PdfFragment.newInstance(transactionsList.getTransactions(), incomes, expenses, date, date2)));

        view.findViewById(R.id.bTomorrow).setOnClickListener(v -> {
            this.date = date.nextMonth();
            this.date2 = this.date2.nextMonth();
            loadTransactions(this.date, this.date2);
        });

        view.findViewById(R.id.bYesterday).setOnClickListener(v -> {
            this.date = date.lastMonth();
            this.date2 = this.date2.lastMonth();
            loadTransactions(this.date, this.date2);
        });

        summaryIncomes = new SummaryListLayout(requireActivity(), view.findViewById(R.id.incomes_frame), decimalFormat, true);
        summaryExpenses = new SummaryListLayout(requireActivity(), view.findViewById(R.id.expenses_frame), decimalFormat, false);
        summaryBalance = new SummaryView(requireActivity(), view.findViewById(R.id.balance_frame));

        summaryIncomes.setOnCategoryClick(this);
        summaryExpenses.setOnCategoryClick(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.navigation_view_summary).setOnClickListener(v -> {});
        loadTransactions(this.date, this.date2);
    }

    @Override
    public void onBackPressed() {
        goHome();
    }
    //</editor-fold>

    /** Carga las transacciones del período seleccionado desde la Base de Datos y, al finalizar, actualiza las vistas. **/
    private void loadTransactions(final Date initDate, final Date endDate) {

        final LinearLayout bShowBalance = requireView().findViewById(R.id.bShowBalance);
        final LinearLayout bShowIncomes = requireView().findViewById(R.id.bShowIncomes);
        final LinearLayout bShowExpenses = requireView().findViewById(R.id.bShowExpenses);
        final TextView summaryEmpty = requireView().findViewById(R.id.summary_list_empty);
        final View loadingView = requireView().findViewById(R.id.loading_view);

        actualizeNavigationButtons();

        final TransactionsViewModel transactionViewModel = new TransactionsViewModel(); //new ViewModelProvider(this).get(TransactionsViewModel.class);

        if (initDate.encode() == endDate.encode())
            transactionViewModel.loadTransactions(requireActivity(), initDate);
        else
            transactionViewModel.loadTransactions(requireActivity(), initDate, endDate);

        // Observa el estado de carga
        transactionViewModel.isDataLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                requireView().findViewById(R.id.floatingButton).setVisibility(View.GONE);
                requireView().findViewById(R.id.transactions).setVisibility(View.GONE);
                loadingView.setVisibility(View.VISIBLE);
                summaryEmpty.setVisibility(View.GONE);
                summaryBalance.hide();
                summaryExpenses.hide();
                summaryIncomes.hide();
            }
        });

        transactionViewModel.getTransactions().observe(getViewLifecycleOwner(), transactionList -> {
            if (transactionViewModel.isSameDate(date, date2)) {

                final Transactions transactions = transactionList.filterCoin(Preferences.defaultCoin(requireActivity()).getName());
                transactionViewModel.removeData();

                transactionsList.set(transactions);

                if (transactions.size() > 0) {

                    // Summary
                    if (expenses && incomes) // Balance
                        showBalance(bShowIncomes, bShowExpenses, bShowBalance);
                    else if (incomes) // Incomes
                        showIncomes(bShowIncomes, bShowExpenses, bShowBalance);
                    else if (expenses) // Expenses
                        showExpenses(bShowIncomes, bShowExpenses, bShowBalance);

                } else {
                    summaryEmpty.setVisibility(View.VISIBLE);
                    requireView().findViewById(R.id.floatingButton).setVisibility(View.GONE);
                    requireView().findViewById(R.id.transactions).setVisibility(View.GONE);
                    loadingView.setVisibility(View.GONE);
                }

                loadSummary(summaryExpenses, transactions.expenses().getCategoriesSorted());
                loadSummary(summaryIncomes, transactions.incomes().getCategoriesSorted());
            }
        });
    }

    private void actualizeTransactionsList(final Transactions transactions) {
        final View progressBar = requireView().findViewById(R.id.transactions_list_progress_bar);
        final View loadingView = requireView().findViewById(R.id.loading_view);
        final View transactionsView = requireView().findViewById(R.id.transactions);

        if (!transactions.isEmpty()) {
            transactionsView.setVisibility(View.VISIBLE);

            final LinearLayoutViewModel inflaterViewModel = new LinearLayoutViewModel(); //new ViewModelProvider(this).get(LinearLayoutViewModel.class);

            inflaterViewModel.isInflating().observe(getViewLifecycleOwner(), isInflating -> {
                if (isInflating) {
                    progressBar.setVisibility(View.VISIBLE);
                    transactionsList.setVisibility(View.GONE);
                }
            });

            inflaterViewModel.setDate(date);
            inflaterViewModel.getLayout().observe(getViewLifecycleOwner(), layout -> {
                inflaterViewModel.detach();
                if (inflaterViewModel.isCurrentDate(date)) {
                    progressBar.setVisibility(View.GONE);
                    transactionsList.setVisibility(View.VISIBLE);
                    loadingView.setVisibility(View.GONE);
                    requireView().findViewById(R.id.floatingButton).setVisibility(View.VISIBLE);
                    transactionsList.showContent(layout);
                }
            });

            inflaterViewModel.showTransactions(requireContext(), transactionsList, transactions);
        } else {
            transactionsView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            loadingView.setVisibility(View.GONE);
        }
    }

    //<editor-fold defaultstate="collapsed" desc=" Showers ">
    /** Actualiza los botones de navegación de fechas, y el nombre de la fecha actual **/
    @SuppressLint("SetTextI18n")
    private void actualizeNavigationButtons() { // used in showTransactions
        final TextView actionBarDate = requireView().findViewById(R.id.summary_date);
        final TextView yesterdayName = requireView().findViewById(R.id.yesterdayName);
        final TextView tomorrowName = requireView().findViewById(R.id.tomorrowName);
        if (date.encode() == date2.encode()) {
            requireView().findViewById(R.id.navigation_buttons).setVisibility(View.VISIBLE);
            yesterdayName.setText(date.lastMonth().getMonthNameAndYearReduced(requireActivity()));
            tomorrowName.setText(date2.nextMonth().getMonthNameAndYearReduced(requireActivity()));
            actionBarDate.setText(date.getMonthNameAndYear(requireActivity()));
        } else {
            requireView().findViewById(R.id.navigation_buttons).setVisibility(View.GONE);
            actionBarDate.setText(date.getDay() != date2.getDay() ?
                    date.formatedDateExtended(requireActivity()) + " -> " + date2.formatedDateExtended(requireActivity()) :
                    date.formatedDateExtended(requireActivity())
            );
        }
    }

    private void showCategoryFragment(CategorySummary fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    private void showPdfFragment(PdfFragment fragment) {
        fragment.setOnFragmentPdfListener(this::hideFragmentAbove);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    private void showDateFragment(final Date dateSelected) {
        DateFragment fragment = DateFragment.newInstance(dateSelected);
        fragment.setOnChangeFragmentListener(this::showFragment);
        fragment.setOnCloseFragmentListener(changesRealized -> {
            if (changesRealized)
                loadTransactions(date, date2);
            hideFragmentAbove();
        });
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    private void showTransactionFragment(TransactionFragment fragment) {
        fragment.setOnTransactionChangeListener(this);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActionPressed() {
        CalendarsFragmentAbove fragment = new CalendarsFragmentAbove();
        fragment.setOnFragmentInteractionListener(new CalendarsFragmentAbove.OnFragmentInteractionListener() {
            @Override
            public void onCloseFragment() {
                hideFragmentAbove();
            }

            @Override
            public void onDateChanged(Date initDate, Date endsDate) {
                hideFragmentAbove();
                if (date.encode() == initDate.encode() && date2.encode() == endsDate.encode()) return;
                date = initDate;
                date2 = endsDate;
                loadTransactions(date, date2);
            }
        });
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    private void showIncomes(final LinearLayout bShowIncomes, final LinearLayout bShowExpenses, final LinearLayout bShowBalance) {
        this.expenses = false;
        this.incomes = true;
        bShowIncomes.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.cardview_background));
        bShowBalance.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.cardview_background_hidden));
        bShowExpenses.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.cardview_background_hidden));
        if (!transactionsList.getTransactions().isEmpty()) {
            summaryExpenses.hide();
            summaryIncomes.show();
            summaryBalance.hide();
        }
        actualizeTransactionsList(transactionsList.getTransactions(true));
    }

    private void showExpenses(final LinearLayout bShowIncomes, final LinearLayout bShowExpenses, final LinearLayout bShowBalance) {
        this.expenses = true;
        this.incomes = false;
        bShowIncomes.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.cardview_background_hidden));
        bShowBalance.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.cardview_background_hidden));
        bShowExpenses.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.cardview_background));
        if (!transactionsList.getTransactions().isEmpty()) {
            summaryExpenses.show();
            summaryIncomes.hide();
            summaryBalance.hide();
        }
        actualizeTransactionsList(transactionsList.getTransactions(false));
    }

    private void showBalance(final LinearLayout bShowIncomes, final LinearLayout bShowExpenses, final LinearLayout bShowBalance) {
        this.expenses = true;
        this.incomes = true;
        bShowIncomes.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.cardview_background_hidden));
        bShowBalance.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.cardview_background));
        bShowExpenses.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.cardview_background_hidden));
        if (!transactionsList.getTransactions().isEmpty()) {
            if (!transactionsList.getTransactions().expenses().isEmpty()) summaryExpenses.show();
            else summaryExpenses.hide();
            if (!transactionsList.getTransactions().incomes().isEmpty()) summaryIncomes.show();
            else summaryIncomes.hide();
            summaryBalance.actualizeBalance(transactionsList.getTransactions().expenses().total(Preferences.defaultCoin(requireActivity())), transactionsList.getTransactions().incomes().total(Preferences.defaultCoin(requireActivity())));
        }
        actualizeTransactionsList(transactionsList.getTransactions());
    }

    private void loadSummary(final SummaryListLayout summary, final Transactions transactions) {

        final int visibility = summary.getVisibility();
        summary.setVisibility(View.VISIBLE);

        final LinearLayoutViewModel inflaterViewModelExpenses = new LinearLayoutViewModel();
        //inflaterViewModelExpenses.isInflating().observe(getViewLifecycleOwner(), isInflating -> { if (isInflating) summary.showLoading(); });
        inflaterViewModelExpenses.getLayout().observe(getViewLifecycleOwner(), layout -> summary.showContent(layout, visibility));
        inflaterViewModelExpenses.showTransactions(requireContext(), summary, transactions, transactions.total(Preferences.defaultCoin(requireActivity())));

    }

    private void actualizeSummaryList(final boolean incomes) {
        if (incomes)
            loadSummary(summaryIncomes, transactionsList.getTransactions().expenses().getCategoriesSorted());
        else loadSummary(summaryExpenses, transactionsList.getTransactions().incomes().getCategoriesSorted());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Implements ">
    @Override
    public void onTransactionCreate(final Transaction transaction) {
        hideFragmentAbove();
        transactionsList.add(transaction);
        actualizeSummaryList(transaction.isAnIncome());
        actualizeTransactionsList();
    }

    @Override
    public void onTransactionDelete(final Transaction transaction) {
        hideFragmentAbove();
        try {
            transactionsList.remove(transaction);
            actualizeSummaryList(transaction.isAnIncome());
            actualizeTransactionsList();
        } catch(Exception ignored) {
            loadTransactions(date, date2);
        }
    }

    @Override
    public void onTransactionEdited(final Transaction oldTransaction, final Transaction newTransaction) {
        hideFragmentAbove();
        try {
            if (oldTransaction.getDate().isSameMonth(newTransaction.getDate()))
                transactionsList.edit(oldTransaction, newTransaction);
            else transactionsList.remove(oldTransaction);
            actualizeSummaryList(oldTransaction.isAnIncome());
            actualizeTransactionsList();
        } catch(Exception ignored) {
            loadTransactions(date, date2);
        }
    }

    @Override
    public void onCategoryClicked(final int row, final Category category) {
        CategorySummary fragment = CategorySummary.newInstance(category.getName(), date, date2);
        fragment.setOnChangeFragmentListener(this::showFragment);
        fragment.setOnFragmentCategoryListener(changesRealized -> {
            hideFragmentAbove();
            if (changesRealized)
                loadTransactions(date, date2);
        });
        showCategoryFragment(fragment);
    }

    @Override
    public void onCancelTransactionOperation() {
        hideFragmentAbove();
    }
    @Override
    public void transactionClicked(int i, int row, final Transaction transaction) {
        showTransactionFragment(TransactionFragment.newInstance(transaction, row));
    }

    @Override
    public void transactionPhotoClicked(int i, int row, final Transaction transaction) {
        TransactionImage transactionImage = TransactionImage.newInstance(transaction);
        transactionImage.setOnFragmentInteractionListener(this::hideFragmentAbove);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, transactionImage);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    @Override
    public void transactionLongClicked(int i, int row, final Transaction transaction) {
        showTransactionFragment(TransactionFragment.newInstance(transaction, true, row));
    }

    @Override
    public void transactionDayClicked(final Date date) {
        showDateFragment(date);
    }

    private void actualizeTransactionsList() {
        actualizeTransactionsList( (incomes && expenses) ? transactionsList.getTransactions() : transactionsList.getTransactions(incomes) );
    }
    //</editor-fold>

}