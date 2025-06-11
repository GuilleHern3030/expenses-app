package enel.dev.budgets.views.editor.transaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.Date;

public class TransactionSelectorFragment extends Fragment {

    public TransactionSelectorFragment() {
        // Required empty public constructor
    }

    private Date date;
    private boolean isDebtAvailable;

    public static TransactionSelectorFragment newInstance(final Date date, final boolean isDebtAvailable) {
        TransactionSelectorFragment newFragment = new TransactionSelectorFragment();
        Bundle bundle = new Bundle();
        if (date != null) bundle.putString("date", date.toString());
        bundle.putBoolean("isdebtavailable", isDebtAvailable);
        newFragment.setArguments(bundle);
        return newFragment;
    }

    public static TransactionSelectorFragment newInstance(final Date date) {
        return newInstance(date, true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try { // 8 arguments
            this.isDebtAvailable = getArguments().getBoolean("isdebtavailable");
            final String dateEncoded = getArguments().getString("date");
            this.date = dateEncoded != null ? new Date(dateEncoded) : new Date();
        } catch(Exception e) {
            Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transaction_add_selector, container, false);

        if (!this.isDebtAvailable)
            view.findViewById(R.id.bAddDebt).setVisibility(View.GONE);

        view.findViewById(R.id.bAddExpense).setOnClickListener(v -> replaceFragment(TransactionFragment.newInstance(date, false)));
        view.findViewById(R.id.bAddIncome).setOnClickListener(v -> replaceFragment(TransactionFragment.newInstance(date, true)));
        view.findViewById(R.id.bAddDebt).setOnClickListener(v -> listener.onDebtOperationRequired());
        view.findViewById(R.id.bExit).setOnClickListener(v -> listener.onCancelOperation());

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
                        listener.onCancelOperation();
                    }
                });
    }

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    private OnTransactionListener listener;
    public interface OnTransactionListener {
        void onTransactionOperationRequired(final TransactionFragment transactionFragment);
        void onDebtOperationRequired();
        void onCancelOperation();
    }

    public void setOnTransactionChangeListener(OnTransactionListener listener) {
        this.listener = listener;
    }

    private void replaceFragment(TransactionFragment fragment) {
        this.listener.onTransactionOperationRequired(fragment);
    }
    //</editor-fold>
}