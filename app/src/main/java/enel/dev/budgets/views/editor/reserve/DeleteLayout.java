package enel.dev.budgets.views.editor.reserve;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.reserve.Reserve;
import enel.dev.budgets.utils.SnackBar;

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

    public static DeleteLayout newInstance(final Reserve debt) {
        DeleteLayout fragment = new DeleteLayout();
        Bundle args = new Bundle();
        args.putInt("id", debt.id());
        args.putString("name", debt.getName());
        args.putDouble("amount", debt.getAmount());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) try {
            this.id = getArguments().getInt("id", -1);
            final double coinAmount = getArguments().getDouble("amount", 0);
            this.lenderName = getArguments().getString("name", "");
            this.money = new Money(Preferences.defaultCoin(requireActivity()), coinAmount);

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
        return inflater.inflate(R.layout.fragment_reserve_delete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView lenderNameText = view.findViewById(R.id.transaction_category_name);
        lenderNameText.setText(lenderName);

        TextView amount = view.findViewById(R.id.transaction_amount);
        amount.setText(money.toString(Preferences.decimalFormat(requireActivity())));

        view.findViewById(R.id.bAccept).setOnClickListener(v -> {
            Controller.reserves(requireActivity()).delete(id, new Controller.SQLcallback() {
                @Override
                public void onSuccess() {
                    if (isAdded())
                        listener.onSuccessDelete();
                }

                @Override
                public void onError(String error) {
                    if (isAdded())
                        SnackBar.show(requireActivity(), getView(), error);
                }

                @Override
                public void onNetworkError() {
                    if (isAdded())
                        SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.network_error));
                }
            });
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