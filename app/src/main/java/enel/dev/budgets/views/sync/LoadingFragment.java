package enel.dev.budgets.views.sync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.data.sql.UserController;
import enel.dev.budgets.utils.SnackBar;
import enel.dev.budgets.views.Fragment;

public class LoadingFragment extends Fragment {

    public static LoadingFragment newInstance() {
        LoadingFragment fragment = new LoadingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.cancel_sync).setOnClickListener(v -> onLoadFinished.onFailure(requireActivity().getString(R.string.sync_canceled)));

        Controller.loadDataBase(requireActivity(), new Controller.LoadCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> onLoadFinished.onSuccess());
                }
            }

            @Override
            public void onError(String error, int errorCode) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (errorCode == 401)
                            UserController.unsync(requireActivity()); // sync code error
                        UserController.setUseCloud(requireActivity(), false);
                        onLoadFinished.onFailure(error);
                        //Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onNetworkError() {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            onLoadFinished.onFailure(requireActivity().getString(R.string.network_error))
                    );
                }
            }
        });
    }

    @Override
    public void onActionPressed() {

    }

    @Override
    public void onBackPressed() {
        requireActivity().finishAffinity();
    }

    private OnLoadFinished onLoadFinished;
    public interface OnLoadFinished {
        void onSuccess();
        void onFailure(final String errorText);
    }
    public void setOnLoadFinishListener(OnLoadFinished listener) {
        this.onLoadFinished = listener;
    }
}
