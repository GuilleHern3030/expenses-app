package enel.dev.budgets.views.editor.transaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transaction;

public class DeleteLayout extends Fragment {

    // Transaction attributes
    protected int id;
    protected Category category;
    protected Date date;
    protected Money money;
    protected String description;
    protected boolean isAnIncome;

    public DeleteLayout() {
        // Required empty public constructor
    }

    public static DeleteLayout newInstance(final Transaction transaction) {
        DeleteLayout fragment = new DeleteLayout();
        Bundle args = new Bundle();
        args.putString("date", transaction.getDate().toString());
        args.putInt("id", transaction.id());
        args.putString("categoryname", transaction.getCategory().getName());
        args.putString("date", transaction.getDate().toString());
        args.putString("coinname", transaction.getMoney().getCoin().getName());
        args.putDouble("coinamount", transaction.getMoney().getAmount());
        args.putBoolean("isanincome", transaction.isAnIncome());
        args.putString("description", transaction.getDescription());
        args.putString("photouri", transaction.getPhotoUri());
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
            this.isAnIncome = getArguments().getBoolean("isanincome", false);

            this.category = categoryName != null ? categories.getCategory(categoryName) : null;
            this.date = dateEncoded != null ? new Date(dateEncoded) : new Date();
            this.money = coinName != null ?
                    new Money(Controller.balances(requireActivity()).get().getCoin(coinName), coinAmount) :
                    new Money(Preferences.defaultCoin(requireActivity()), coinAmount);

        } catch(Exception e) {
            listener.onCancelDelete();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        listener.onCancelDelete();
                    }
                });
        return inflater.inflate(R.layout.fragment_transaction_delete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout transactionBackground = view.findViewById(R.id.transaction_background);
        transactionBackground.setBackgroundResource(isAnIncome ? R.color.income_background : R.color.expense_background);

        FrameLayout categoryBackground = view.findViewById(R.id.transaction_category_container);
        categoryBackground.setBackgroundResource(category.getColor());

        ImageView categoryImage = view.findViewById(R.id.transaction_category_image);
        categoryImage.setImageResource(category.getImage());

        TextView categoryName = view.findViewById(R.id.transaction_category_name);
        categoryName.setText(category.getName());

        TextView descriptionText = view.findViewById(R.id.transaction_description);
        descriptionText.setText(description);

        TextView amount = view.findViewById(R.id.transaction_amount);
        amount.setText(money.toString(Preferences.decimalFormat(requireActivity())));

        view.findViewById(R.id.bAccept).setOnClickListener(v -> {
            Controller.transactions(requireActivity()).delete(id, date.toString());
            // Controller.deleteTransactionPhoto(requireActivity(), id, date.toString()); // TODO
            listener.onSuccessDelete();
        });

        view.findViewById(R.id.bCancel).setOnClickListener(v -> listener.onCancelDelete());
    }



    private OnDeleteListener listener;
    public interface OnDeleteListener {
        void onSuccessDelete();
        void onCancelDelete();
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.listener = listener;
    }


}