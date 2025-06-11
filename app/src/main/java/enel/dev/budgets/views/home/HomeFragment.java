package enel.dev.budgets.views.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import enel.dev.budgets.R;
import enel.dev.budgets.data.livedata.BalanceViewModel;
import enel.dev.budgets.data.livedata.DebtsViewModel;
import enel.dev.budgets.data.livedata.LinearLayoutViewModel;
import enel.dev.budgets.data.livedata.TransactionsViewModel;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.debt.Debts;
import enel.dev.budgets.objects.money.Balance;
import enel.dev.budgets.objects.debt.Debt;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;

import enel.dev.budgets.views.Fragment;
import enel.dev.budgets.views.configuration.ConfigurationFragment;
import enel.dev.budgets.views.date.DateFragment;
import enel.dev.budgets.views.editor.debt.DebtFragment;
import enel.dev.budgets.views.editor.transaction.TransactionFragment;
import enel.dev.budgets.views.editor.transaction.TransactionSelectorFragment;
import enel.dev.budgets.views.categorysummary.CategorySummary;
import enel.dev.budgets.views.editor.transaction.TransactionImage;
import enel.dev.budgets.views.summary.SummaryListLayout;

public class HomeFragment extends Fragment implements TransactionFragment.OnTransactionChangeListener, DebtFragment.OnDebtChangeListener {

    //<editor-fold defaultstate="collapsed" desc=" Constructor ">

    private LinearLayout bShowExpenses; // show expenses button
    private LinearLayout bShowIncomes; // show incomes button

    private TextView transactionsBalance; // Total de transacciones del día
    private FrameLayout transactionsBalanceContainer; // Total de transacciones del día (frame)

    private TransactionsListLayout transactionsList; // layout de la lista de las transacciones del día
    private SummaryListLayout summaryListExpenses; // layout de la lista del resumen de transacciones del mes dividido por categorías
    private SummaryListLayout summaryListIncomes; // layout de la lista del resumen de transacciones del mes dividido por categorías
    private BalanceListLayout balanceList; // layout de la lista del balance de monedas
    private DebtList debtList; // layout de la lista de deudas

    private Transactions transactionsLoaded; // lista cargada de todas las transacciones del mes
    private Balance balanceLoaded; // lista cargada de todas las transacciones del mes

    public static HomeFragment newInstance(String... params) {
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(bundle(params));
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            fragmentAbove = view.findViewById(R.id.fragment_above);

            bShowExpenses = view.findViewById(R.id.bShowExpenses);
            bShowIncomes = view.findViewById(R.id.bShowIncomes);

            transactionsBalance = view.findViewById(R.id.transactions_day_balance);
            transactionsBalanceContainer = view.findViewById(R.id.transactions_day_balance_frame);

            view.findViewById(R.id.bConfig).setOnClickListener(v -> replaceFragment(new ConfigurationFragment()));

            TextView summaryTitle = view.findViewById(R.id.summaryTitle);
            summaryTitle.setText(new Date().getMonthNameAndYear(requireActivity()));

            createCalendar(view); // Calendar

            // Lists
            final NumberFormat decimalFormat = Preferences.decimalFormat(requireActivity());
            balanceList = new BalanceListLayout(requireContext(), view.findViewById(R.id.balance_list), decimalFormat, Preferences.defaultCoin(requireActivity()));
            summaryListExpenses = new SummaryListLayout(requireActivity(), view.findViewById(R.id.summary_list), decimalFormat);
            summaryListIncomes = new SummaryListLayout(requireActivity(), view.findViewById(R.id.summary_list_incomes), decimalFormat);
            transactionsList = new TransactionsListLayout(requireContext(), view.findViewById(R.id.transactions_list), decimalFormat);
            debtList = new DebtList(requireContext(), view.findViewById(R.id.debt_list), decimalFormat);

        } catch (Exception e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadTransactions(); // Carga las transacciones del mes desde la Base de Datos y, al finalizar, actualiza las vistas.
        loadDebts(); // Carga las deudas existentes
        view.findViewById(R.id.navigation_view_home).setOnClickListener(v -> {});

        transactionsList.setOnTransactionCLick(new TransactionsListLayout.OnElementClickListener() {
            @Override
            public void transactionClicked(int index, Transaction transaction) {
                TransactionFragment transactionFragment = TransactionFragment.newInstance(transaction, index);
                showTransactionFragment(transactionFragment);
            }

            @Override
            public boolean transactionLongClicked(int index, Transaction transaction) {
                TransactionFragment transactionFragment = TransactionFragment.newInstance(transaction, true, index);
                showTransactionFragment(transactionFragment);
                return true;
            }

            @Override
            public void transactionPhoto(int index, Transaction transaction) {
                TransactionImage fragment = TransactionImage.newInstance(transaction);
                fragment.setOnFragmentInteractionListener(() -> hideFragmentAbove());
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_above, fragment);
                fragmentTransaction.commit();
                fragmentAbove.setVisibility(View.VISIBLE);

            }
        });

        summaryListExpenses.setOnCategoryClick((i, category) -> showCategoryFragment(category));
        summaryListIncomes.setOnCategoryClick((i, category) -> showCategoryFragment(category));

        bShowExpenses.setOnClickListener(v -> showSummaryList(false));
        bShowIncomes.setOnClickListener(v -> showSummaryList(true));

    }

    @Override
    public void onBackPressed() {
        requireActivity().finishAffinity();
    }
    //</editor-fold>

    /** Carga las deudas desde la Base de Datos y, al finalizar, actualiza la vista de deudas */
    private void loadDebts() {
        DebtsViewModel debtsViewModel = new ViewModelProvider(this).get(DebtsViewModel.class);

        // Observa el estado de carga
        debtsViewModel.isDataLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading)
                debtList.showLoading();
        });

        // Observa las transacciones cargadas
        debtsViewModel.getDebts().observe(getViewLifecycleOwner(), this::actualizeDebtList);

        // Cargar las transacciones
        debtsViewModel.loadDebts(requireActivity());
    }

    /** Carga las transacciones del mes actual desde la Base de Datos y, al finalizar, actualiza las vistas */
    private void loadTransactions() {
        balanceLoaded = null;

        TransactionsViewModel transaccionViewModel = new ViewModelProvider(this).get(TransactionsViewModel.class);

        // Observa el estado de carga
        transaccionViewModel.isDataLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                transactionsList.showLoading();
                summaryListExpenses.showLoading();
                summaryListIncomes.showLoading();
                balanceList.showLoading();
            }
        });

        // Observa las transacciones cargadas
        transaccionViewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            transactionsLoaded = transactions;
            transaccionViewModel.removeData();
            actualizeViews(transactions);
        });

        // Cargar las transacciones
        transaccionViewModel.loadTransactions(requireActivity());
    }

    /**
     * Actualiza todas las vistas con las transacciones que reciba como parámetro,
     * con excepción de la vista de Balance, la cual comenzará a cargarse tras actualizarse las otras vistas.
     */
    private void actualizeViews(final Transactions allTransactions) {
        final Transactions transactions = allTransactions.filterCoin(Preferences.defaultCoin(requireActivity()).getName());

        final Transactions incomes = transactions.incomes();
        final Transactions expenses = transactions.expenses();

        actualizeSummaryList(transactions);

        actualizeDayTransactionsList(transactions.today(new Date()));

        actualizeBalanceOfMonth(incomes, expenses);

        if (balanceLoaded == null)
            loadBalance(allTransactions);

    }

    /**
     * Carga el balance de monedas desde la Base de Datos en caso de que exista más de una moneda configurada y,
     * al finalizar, actualiza la vista de Balance. Requiere que las Transacciones ya estén cargadas.
     */
    private void loadBalance(final Transactions transactions) {
        BalanceViewModel viewModel = new ViewModelProvider(this).get(BalanceViewModel.class);

        viewModel.isDataLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading)
                balanceList.showLoading();
        });

        viewModel.getBalance().observe(getViewLifecycleOwner(), balances -> {
            balanceLoaded = balances;
            actualizeBalanceList(balances, transactions);
        });

        viewModel.loadBalance(requireActivity());
    }

    //<editor-fold defaultstate="collapsed" desc=" SelectorFragment ">
    @Override
    public void onActionPressed() {
        TransactionSelectorFragment transactionFragment = TransactionSelectorFragment.newInstance(new Date());
        transactionFragment.setOnTransactionChangeListener(new TransactionSelectorFragment.OnTransactionListener() {
            @Override
            public void onTransactionOperationRequired(TransactionFragment transactionFragment) {
                showTransactionFragment(transactionFragment);
            }

            @Override
            public void onDebtOperationRequired() {
                hideFragmentAbove();
                showDebtFragment(DebtFragment.newInstance());
            }

            @Override
            public void onCancelOperation() {
                hideFragmentAbove();
            }
        });
        showFragmentAbove(transactionFragment);
    }

    private void showTransactionFragment(TransactionFragment fragment) {
        fragment.setOnTransactionChangeListener(this);
        showFragmentAbove(fragment);
    }

    private void showDebtFragment(DebtFragment fragment) {
        fragment.setOnDebtChangeListener(this);
        showFragmentAbove(fragment);
    }

    private void showDateFragment(DateFragment fragment) {
        fragment.setOnChangeFragmentListener(this::showFragment);
        fragment.setOnCloseFragmentListener(changesRealized -> {
            if (changesRealized)
                loadTransactions();
            hideFragmentAbove();
        });
        showFragmentAbove(fragment);
    }

    private void showCategoryFragment(Category category) {
        CategorySummary categorySummary = CategorySummary.newInstance(category.getName(), new Date());
        categorySummary.setOnChangeFragmentListener(this::showFragment);
        categorySummary.setOnFragmentCategoryListener(changesRealized -> {
            if (changesRealized)
                loadTransactions();
            hideFragmentAbove();
        });
        showFragmentAbove(categorySummary);
    }

    private void showFragmentAbove(androidx.fragment.app.Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Views ">
    private void createCalendar(final View view) {

        CardView layout = view.findViewById(R.id.calendar);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {

            try {
                showDateFragment(DateFragment.newInstance(new Date(year, (month + 1), dayOfMonth)));
            } catch(Exception e) {
                Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_LONG).show();
            }

        });

        layout.setVisibility(View.VISIBLE);

    }

    private void actualizeBalanceOfMonth(final Transactions incomes, final Transactions expenses) {
        final Date date = new Date();
        ((TextView)requireView().findViewById(R.id.movementsExpensesTotal)).setText(expenses.total(Preferences.defaultCoin(requireActivity())).toString(Preferences.decimalFormat(requireActivity())));
        ((TextView)requireView().findViewById(R.id.movementsExpensesToday)).setText(expenses.totalToday(Preferences.defaultCoin(requireActivity()), date).toString(Preferences.decimalFormat(requireActivity())));
        ((TextView)requireView().findViewById(R.id.movementsIncomesTotal)).setText(incomes.total(Preferences.defaultCoin(requireActivity())).toString(Preferences.decimalFormat(requireActivity())));
        ((TextView)requireView().findViewById(R.id.movementsIncomesToday)).setText(incomes.totalToday(Preferences.defaultCoin(requireActivity()), date).toString(Preferences.decimalFormat(requireActivity())));
    }

    private void actualizeDebtList(final Debts allDebts) {

        final Debts debts = allDebts.filterCoin(Preferences.defaultCoin(requireActivity()).getName());

        if (debts.size() > 0) {
            debtList.showContent(debts);
            debtList.setOnDebtCLick(new DebtList.OnElementClickListener() {
                @Override
                public void debtClicked(int row, Debt debt) {
                    showDebtFragment(DebtFragment.newInstance(debt, row));
                }

                @Override
                public boolean debtLongClicked(int row, Debt debt) {
                    showDebtFragment(DebtFragment.newInstance(debt, row, true));
                    return true;
                }
            });
            requireView().findViewById(R.id.debts_card_view).setVisibility(View.VISIBLE);
        } else requireView().findViewById(R.id.debts_card_view).setVisibility(View.GONE);
    }

    private void actualizeBalanceList(final Balance balance, final Transactions transactions) {

        if (Preferences.moreCoinsAvailable(requireActivity()))
            balanceList.showContent(balance, transactions);
        else
            balanceList.showContent(transactions);

        balanceList.setOnBalanceCLick((row, money) -> {
            Preferences.setDefaultCoin(requireActivity(), money.getCoin());
            balanceList.setDefaultCoin(money.getCoin());
            actualizeViews(transactions);
            actualizeBalanceList(balanceLoaded, transactions);
            loadDebts();
        });

    }

    private void actualizeDayTransactionsList(final Transactions transactions) {

        final LinearLayoutViewModel inflaterViewModel = new ViewModelProvider(this).get(LinearLayoutViewModel.class);
        inflaterViewModel.isInflating().observe(getViewLifecycleOwner(), isInflating -> {
            if (isInflating)
                transactionsList.showLoading();
        });

        inflaterViewModel.getLayout().observe(getViewLifecycleOwner(), layout -> transactionsList.showContent(layout));

        inflaterViewModel.showTransactions(requireContext(), transactionsList, transactions);

        if (transactions.size() > 0) {
            transactionsBalanceContainer.setVisibility(View.VISIBLE);
            transactionsBalance.setText(transactions.balance().total(Preferences.defaultCoin(requireActivity())).toString(Preferences.decimalFormat(requireActivity())));
        } else transactionsBalanceContainer.setVisibility(View.GONE);

    }

    private void actualizeSummaryList(final Transactions summary) {
        actualizeSummaryList(summaryListExpenses, summary.expenses().getCategoriesSorted());
        actualizeSummaryList(summaryListIncomes, summary.incomes().getCategoriesSorted());
    }

    private void actualizeSummaryList(final SummaryListLayout listLayout, final Transactions transactions) {
        final int visibility = listLayout.getVisibility();
        listLayout.setVisibility(View.VISIBLE);
        final LinearLayoutViewModel inflaterViewModelIncomes = new LinearLayoutViewModel(); //new ViewModelProvider(this).get(LinearLayoutViewModel.class);
        inflaterViewModelIncomes.isInflating().observe(getViewLifecycleOwner(), isInflating -> { if (isInflating) summaryListIncomes.showLoading(); });
        inflaterViewModelIncomes.getLayout().observe(getViewLifecycleOwner(), layout -> listLayout.showContent(layout, visibility));
        inflaterViewModelIncomes.showTransactions(requireContext(), listLayout, transactions, transactions.total(Preferences.defaultCoin(requireActivity())));
    }

    private void showSummaryList(final boolean incomes) {
        bShowIncomes.setBackgroundColor(ContextCompat.getColor(requireActivity(), incomes ? R.color.cardview_background : R.color.cardview_background_hidden));
        bShowExpenses.setBackgroundColor(ContextCompat.getColor(requireActivity(), incomes ? R.color.cardview_background_hidden : R.color.cardview_background));
        summaryListExpenses.setVisibility(incomes ? View.GONE : View.VISIBLE);
        summaryListIncomes.setVisibility(incomes ? View.VISIBLE : View.GONE);
    }
    //</editor-fold>

    @Override
    public void onTransactionCreate(Transaction transaction) {
        hideFragmentAbove();
        transactionsLoaded.add(transaction);
        balanceLoaded.add(transaction);
        actualizeViews(transactionsLoaded);
        actualizeBalanceList(balanceLoaded, transactionsLoaded);
    }

    @Override
    public void onTransactionDelete(Transaction transaction) {
        hideFragmentAbove();
        balanceLoaded.remove(transaction);
        transactionsLoaded.remove(transaction);
        actualizeViews(transactionsLoaded);
        actualizeBalanceList(balanceLoaded, transactionsLoaded);
    }

    @Override
    public void onTransactionEdited(final Transaction oldTransaction, final Transaction newTransaction) {
        hideFragmentAbove();
        balanceLoaded.edit(oldTransaction, newTransaction);
        transactionsLoaded.set(oldTransaction, newTransaction);
        actualizeViews(transactionsLoaded);
        actualizeBalanceList(balanceLoaded, transactionsLoaded);
    }

    @Override
    public void onCancelTransactionOperation() {
        hideFragmentAbove();
    }

    @Override
    public void onDebtChanged() {
        hideFragmentAbove();
        loadDebts();
    }

    @Override
    public void onCancelDebtOperation() {
        hideFragmentAbove();
    }
}