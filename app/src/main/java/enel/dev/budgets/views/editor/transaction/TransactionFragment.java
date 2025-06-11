package enel.dev.budgets.views.editor.transaction;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.utils.SnackBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionFragment extends Fragment {

    //<editor-fold defaultstate="collapsed" desc=" Constructor ">
    private FrameLayout fragmentAbove;

    // Transaction attributes
    protected int id;
    protected Category category;
    protected Date date;
    protected Money money;
    protected String description;
    protected boolean isAnIncome;
    protected String photoUri;

    private boolean deleteOption = false;

    private Transaction oldTransaction;

    private TextView categoryName;
    private FrameLayout categoryColor;
    private ImageView categoryImage;

    public TransactionFragment() {
        // Required empty public constructor
    }

    // Edit a exists transaction of a list
    public static TransactionFragment newInstance(final Transaction transaction, final int index) {
        return newInstance(transaction, false, index);
    }

    // Create transaction with a specific date
    public static TransactionFragment newInstance(final Date date, final boolean isAnIncome) {
        TransactionFragment fragment = new TransactionFragment();
        Bundle args = new Bundle();
        args.putString("date", date.toString());
        args.putBoolean("isanincome", isAnIncome);
        fragment.setArguments(args);
        return fragment;
    }

    // Create transaction with today date
    public static TransactionFragment newInstance(final boolean isAnIncome) {
        TransactionFragment fragment = new TransactionFragment();
        Bundle args = new Bundle();
        args.putBoolean("isanincome", isAnIncome);
        fragment.setArguments(args);
        return fragment;
    }

    public static TransactionFragment newInstance(final Transaction transaction, final boolean delete, final int index) {
        TransactionFragment fragment = new TransactionFragment();
        Bundle args = new Bundle();
        args.putInt("id", transaction.id());
        args.putInt("index", index);
        args.putString("categoryname", transaction.getCategory().getName());
        args.putString("date", transaction.getDate().toString());
        args.putString("coinname", transaction.getMoney().getCoin().getName());
        args.putDouble("coinamount", transaction.getMoney().getAmount());
        args.putBoolean("isanincome", transaction.isAnIncome());
        args.putString("description", transaction.getDescription());
        args.putString("photouri", transaction.getPhotoUri());
        args.putBoolean("delete", delete);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Categories categories = Controller.categories(requireActivity()).get();

        if (getArguments() != null) try {
            this.id = getArguments().getInt("id", -1);
            final String categoryName = getArguments().getString("categoryname");
            final String dateEncoded = getArguments().getString("date");
            final String coinName = getArguments().getString("coinname", Preferences.defaultCoin(requireActivity()).getName());
            final double coinAmount = getArguments().getDouble("coinamount", 0);
            this.description = getArguments().getString("description", "");
            this.photoUri = getArguments().getString("photouri", "");
            this.isAnIncome = getArguments().getBoolean("isanincome", false);

            Log.i("TransactionFragment", "id = " + this.id);
            Log.i("TransactionFragment", "categoryname = " + categoryName);
            Log.i("TransactionFragment", "date = " + dateEncoded);
            Log.i("TransactionFragment", "coinname = " + coinName);
            Log.i("TransactionFragment", "coinamount = " + coinAmount);
            Log.i("TransactionFragment", "description = " + this.description);
            Log.i("TransactionFragment", "photouri = " + this.photoUri);
            Log.i("TransactionFragment", "isanincome = " + this.isAnIncome);
            Log.i("TransactionFragment", "delete = " + this.deleteOption);

            this.category = categoryName != null ? categories.getCategory(categoryName) : null;
            this.date = dateEncoded != null ? new Date(dateEncoded) : new Date();
            this.money = coinName != null ?
                    new Money(Controller.balances(requireActivity()).get().getCoin(coinName), coinAmount) :
                    new Money(Preferences.defaultCoin(requireActivity()), coinAmount);

            this.deleteOption = getArguments().getBoolean("delete", false);

            try { this.oldTransaction = new Transaction(id, category.clone(), new Date(date.encode()), money.clone(), description, isAnIncome, photoUri); } catch (Exception ignored) { }

        } catch(Exception e) {
            Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }

        if (this.date == null)
            this.date = new Date();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        cancelOperation();
                    }
                });

        return view;
    }
    //</editor-fold>

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //<editor-fold defaultstate="collapsed" desc=" Views configuration ">
        categoryName = view.findViewById(R.id.transaction_category_name);
        categoryColor = view.findViewById(R.id.transaction_category_image_container);
        categoryImage = view.findViewById(R.id.transaction_category_image);

        final ImageView photoExists = view.findViewById(R.id.photo_exists);
        final TextView inputText = view.findViewById(R.id.transaction_amount);
        final TextView descriptionText = view.findViewById(R.id.description_text);
        final TextView dateText = view.findViewById(R.id.date);

        @ColorInt int incomeTextColor = ContextCompat.getColor(requireActivity(), R.color.income_text);
        @ColorInt int expenseTextColor = ContextCompat.getColor(requireActivity(), R.color.expense_text);

        FrameLayout inputContainer = view.findViewById(R.id.calculator_container);
        inputContainer.setBackgroundResource(isAnIncome ? R.drawable.border_input_income : R.drawable.border_input_expense);
        inputText.setTextColor(isAnIncome ? incomeTextColor : expenseTextColor);
        inputText.setText(money != null ?
                money.toString(Preferences.decimalFormat(requireActivity())) :
                new Money(0).toString(Preferences.decimalFormat(requireActivity())));

        dateText.setText(date.formatedDateExtended(requireActivity()));

        fragmentAbove = view.findViewById(R.id.secondary_transaction_fragment);

        if (photoUri != null && photoUri.length() > 0)
            photoExists.setVisibility(View.VISIBLE);

        if (description != null && description.length() > 0)
            descriptionText.setText(description);

        if (id != -1) {
            view.findViewById(R.id.bDelete).setVisibility(View.VISIBLE);
            view.findViewById(R.id.bDelete).setOnClickListener(v -> showTransactionDeleteDialog());
            if (deleteOption) showTransactionDeleteDialog();
        }

        if (category != null) {
            categoryName.setText(category.getName());
            categoryColor.setBackgroundResource(category.getColor());
            categoryImage.setImageResource(category.getImage());
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc=" Description input ">
        view.findViewById(R.id.description_container).setOnClickListener(v -> {
            EditTextLayout fragment = EditTextLayout.newInstance(description);
            fragment.setOnDescriptionChangeListener(new EditTextLayout.OnDescriptionChangeListener() {
                @Override
                public void onEditDescription(String newDescription) {
                    description = newDescription;
                    descriptionText.setText(newDescription);
                    hideFragmentAbove();
                }

                @Override
                public void onCancelEdition() {
                    hideFragmentAbove();
                }
            });
            showFragment(fragment);
        });
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc=" Camera button ">
        view.findViewById(R.id.bCamera).setOnClickListener(v -> {
            CameraLayout fragment = CameraLayout.newInstance(transaction());
            fragment.setOnTransactionPhotoListener(new CameraLayout.OnTransactionPhotoListener() {
                @Override
                public void onPhotoChanged(String newPhotoUri) {
                    photoUri = newPhotoUri;
                    if (photoUri != null && photoUri.length() > 0)
                        photoExists.setVisibility(View.VISIBLE);
                    else photoExists.setVisibility(View.GONE);
                    hideFragmentAbove();
                }

                @Override
                public void onCancelOperation() {
                    hideFragmentAbove();
                    if (photoUri != null && photoUri.length() > 0)
                        photoExists.setVisibility(View.VISIBLE);
                }
            });
            showFragment(fragment);
        });
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc=" Calendar button ">
        view.findViewById(R.id.bCalendar).setOnClickListener(v -> {
            CalendarLayout fragment = CalendarLayout.newInstance(date);
            fragment.setOnTransactionDateListener(new CalendarLayout.OnTransactionDateListener() {
                @Override
                public void onDateChanged(Date newDate) {
                    date = newDate;
                    dateText.setText(newDate.formatedDateExtended(requireActivity()));
                    hideFragmentAbove();
                }

                @Override
                public void onCancelOperation() {
                    hideFragmentAbove();

                }
            });
            showFragment(fragment);
        });
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc=" Save button ">
        view.findViewById(R.id.bCreateTransaction).setOnClickListener(v -> {
            if (id >= 0) editTransaction(view);
            else createTransaction(view);
        });
        //</editor-fold>

        view.findViewById(R.id.category_container).setOnClickListener(v -> showCategoriesList());
        view.findViewById(R.id.calculator_container).setOnClickListener(v -> showAmountInput(inputText));
        view.findViewById(R.id.bCancelOperation).setOnClickListener(v -> cancelOperation());

        if (money == null || money.getAmount() == 0) // force money set
            showAmountInput(inputText);

    }

    private void hideFragmentAbove() {
        fragmentAbove.setVisibility(View.GONE);
        fragmentAbove.removeAllViews();
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        cancelOperation();
                    }
                });
    }

    private void createTransaction(final View view) {
        if (this.money != null && this.money.getAmount() > 0) {
            Transaction newTransaction = transaction();
            boolean success = Controller.transactions(requireActivity()).add(newTransaction);
            if (success) {
                listener.onTransactionCreate(newTransaction);
            } else {
                SnackBar.show(requireActivity(), view, requireActivity().getString(R.string.transaction_created_failed));
                this.id = -1;
            }
        } else SnackBar.show(requireActivity(), view, requireActivity().getString(R.string.error_invalid_amount));
    }

    private void editTransaction(final View view) {
        Transaction newTransaction = transaction();
        try {
            if (!oldTransaction.getDate().isSameMonth(newTransaction.getDate())) {
                final int newId = Controller.transactions(requireActivity()).move(newTransaction, oldTransaction.getDate(), newTransaction.getDate());
                if (newId >= 0) newTransaction = transaction(newId); else throw new IndexOutOfBoundsException();
            }
            final boolean success = Controller.transactions(requireActivity()).edit(newTransaction);
            if (!success) throw new IllegalArgumentException();
            listener.onTransactionEdited(oldTransaction, newTransaction);
        } catch (IndexOutOfBoundsException ignored) {
            SnackBar.show(requireActivity(), view, requireActivity().getString(R.string.transaction_edited_failed));
            SnackBar.show(requireActivity(), view, "Index out of bounds exception");
        } catch (IllegalArgumentException ignored) {
            SnackBar.show(requireActivity(), view, requireActivity().getString(R.string.transaction_edited_failed));
            SnackBar.show(requireActivity(), view, "Illegal argument exception");
        }
    }

    private void showTransactionDeleteDialog() {
        DeleteLayout fragment = DeleteLayout.newInstance(transaction());
        fragment.setOnDeleteListener(new DeleteLayout.OnDeleteListener() {
            @Override
            public void onSuccessDelete() {
                listener.onTransactionDelete(oldTransaction);
            }

            @Override
            public void onCancelDelete() {
                hideFragmentAbove();
                if (deleteOption)
                    cancelOperation();
            }
        });
        showFragment(fragment);
    }

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    private OnTransactionChangeListener listener;
    public interface OnTransactionChangeListener {
        void onTransactionCreate(final Transaction transaction);
        void onTransactionDelete(final Transaction transaction);
        void onTransactionEdited(final Transaction oldTransaction, final Transaction newTransaction);
        void onCancelTransactionOperation();
    }

    public void setOnTransactionChangeListener(OnTransactionChangeListener listener) {
        this.listener = listener;
    }

    private void cancelOperation() {
        listener.onCancelTransactionOperation();
    }
    //</editor-fold>

    private Transaction transaction() {
        final int id = this.id != -1 ? this.id : Controller.transactions(requireActivity()).get(this.date).getUnusedId();
        return new Transaction(id, category, date, money, description, isAnIncome, photoUri);
    }

    private Transaction transaction(final int id) {
        return new Transaction(id, category, date, money, description, isAnIncome, photoUri);
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.secondary_transaction_fragment, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

    private void showAmountInput(final TextView inputText) {
        CalculatorLayout fragment = money != null ?
                CalculatorLayout.newInstance(isAnIncome, money.getAmount(), money.getCoin().getSymbol()):
                CalculatorLayout.newInstance(isAnIncome, Preferences.defaultCoin(requireActivity()).getSymbol());
        fragment.setOnCalculatorListener(new CalculatorLayout.OnCalculatorListener() {
            @Override
            public void onAccept(double amount) {
                final boolean isMoneyUndefined = money == null || money.getAmount() == 0;
                if (isMoneyUndefined) money = new Money(Preferences.defaultCoin(requireActivity()), amount);
                else money.setAmount(amount);
                inputText.setText(money.toString(Preferences.decimalFormat(requireActivity())));
                if (isMoneyUndefined) showCategoriesList();
                else hideFragmentAbove();
            }

            @Override
            public void onCancel() {
                if (money == null || money.getAmount() == 0)
                    cancelOperation();
                hideFragmentAbove();
            }
        });
        showFragment(fragment);
    }

    private void showCategoriesList() {
        CategoryLayout fragment = category == null ?
                CategoryLayout.newInstance(isAnIncome) :
                CategoryLayout.newInstance(category);
        fragment.setOnTransactionCategoryListener(new CategoryLayout.OnTransactionCategoryListener() {
            @Override
            public void onCategorySelected(Category newCategory) {
                category = newCategory;
                categoryName.setText(newCategory.getName());
                categoryColor.setBackgroundResource(newCategory.getColor());
                categoryImage.setImageResource(newCategory.getImage());
                hideFragmentAbove();
            }

            @Override
            public void onCancelSelection() {
                if (category == null)
                    cancelOperation();
                hideFragmentAbove();
            }
        });
        showFragment(fragment);
    }

}