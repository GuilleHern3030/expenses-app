package enel.dev.budgets.views.sync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.data.sql.UserController;

public class PullFragment extends SyncFragmentContext {

    public PullFragment() {
        // Required empty public constructor
    }

    private Switch switch_use_cloud;
    private View pbLoading;
    private TextView tvError;
    private TextView tvSuccess;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_pull, container, false);

        view.findViewById(R.id.bBack).setOnClickListener(v -> back());

        pbLoading = view.findViewById(R.id.pbPulling);

        tvError = view.findViewById(R.id.tvError);
        tvSuccess = view.findViewById(R.id.tvSuccess);

        switch_use_cloud = view.findViewById(R.id.switch_use_cloud);

        switch_use_cloud.setChecked(UserController.useCloud(requireActivity()));

        switch_use_cloud.setOnCheckedChangeListener((compoundButton, b) -> {
            UserController.setUseCloud(requireActivity(), b);
            tvError.setVisibility(View.GONE);
            tvSuccess.setVisibility(View.GONE);
            if (b)
                pullDataBase();
        });


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (UserController.useCloud(requireActivity()) && UserController.isSynchronized(requireActivity()))
            pullDataBase();
    }

    private void showLoading() {
        switch_use_cloud.setClickable(false);
        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);
        pbLoading.setVisibility(View.VISIBLE);
    }

    private void hideLoading(final boolean success, final String text) {
        switch_use_cloud.setClickable(true);
        pbLoading.setVisibility(View.GONE);
        if (success) {
            tvSuccess.setVisibility(text.length() > 0 ? View.VISIBLE : View.GONE);
            tvSuccess.setText(text);
        } else {
            tvError.setVisibility(text.length() > 0 ? View.VISIBLE : View.GONE);
            tvError.setText(text);
        }
    }

    private void pullDataBase() {
        showLoading();
        Controller.loadDataBase(requireActivity(), new Controller.LoadCallback() {
            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> {
                    hideLoading(true, requireActivity().getString(R.string.sync_synchronized));

                });
            }

            @Override
            public void onError(String error, int errorCode) {
                requireActivity().runOnUiThread(() -> {
                    if (errorCode == 401) UserController.unsync(requireActivity()); // sync code error
                    hideLoading(false, error);
                });
            }

            @Override
            public void onNetworkError() {
                requireActivity().runOnUiThread(() -> {
                    hideLoading(false, requireActivity().getString(R.string.network_error));
                });
            }
        }, true);
    }

}
