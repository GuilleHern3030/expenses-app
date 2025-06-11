package enel.dev.budgets.views.editor.debt;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.debt.Debt;
import enel.dev.budgets.objects.money.Money;

public class DeleteLayout extends Fragment {

    // Transaction attributes
    protected int id;
    protected Money money;
    protected String description;
    protected String lenderName;
    protected boolean isAnIncome;

    public DeleteLayout() {
        // Required empty public constructor
    }

    public static DeleteLayout newInstance(final Debt debt) {
        DeleteLayout fragment = new DeleteLayout();
        Bundle args = new Bundle();
        args.putInt("id", debt.id());
        args.putString("lendername", debt.getLender());
        args.putString("coinname", debt.getMoney().getCoin().getName());
        args.putDouble("coinamount", debt.getMoney().getAmount());
        args.putString("description", debt.getDescription());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) try {
            this.id = getArguments().getInt("id", -1);
            final String coinName = getArguments().getString("coinname", Preferences.defaultCoin(requireActivity()).getName());
            final double coinAmount = getArguments().getDouble("coinamount", 0);
            this.description = getArguments().getString("description", "");
            this.lenderName = getArguments().getString("lendername", "");
            this.isAnIncome = getArguments().getBoolean("isanincome", false);
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
        return inflater.inflate(R.layout.fragment_debt_delete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView lenderNameText = view.findViewById(R.id.transaction_category_name);
        lenderNameText.setText(lenderName);

        TextView descriptionText = view.findViewById(R.id.transaction_description);
        descriptionText.setText(description);

        TextView amount = view.findViewById(R.id.transaction_amount);
        amount.setText(money.toString(Preferences.decimalFormat(requireActivity())));

        view.findViewById(R.id.bAccept).setOnClickListener(v -> {
            Controller.debts(requireActivity()).delete(id);
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