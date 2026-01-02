package enel.dev.budgets.views.sync;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.data.sql.UserController;
import enel.dev.budgets.data.sql.external.synchronize.Desynchronize;
import enel.dev.budgets.data.sql.external.synchronize.Synchronize;

public class SynchronizeFragment extends SyncFragmentContext {

    public SynchronizeFragment() {
        // Required empty public constructor
    }

    private View unsynchronize_layout;
    private View synchronize_layout;
    private View bSubmit;
    private View progressBar;
    private TextView errorText;
    private TextView syncText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        final View view = inflater.inflate(R.layout.fragment_synchronize, container, false);

        final EditText et = view.findViewById(R.id.etCode);
        if (Build.MODEL.startsWith("sdk_"))
            et.setText("INV-TEST");

        view.findViewById(R.id.bBack).setOnClickListener(v -> back());

        synchronize_layout = view.findViewById(R.id.unsynchronized_layout);
        unsynchronize_layout = view.findViewById(R.id.synchronized_layout);

        bSubmit = view.findViewById(R.id.bSync);
        progressBar = view.findViewById(R.id.pbSync);
        errorText = view.findViewById(R.id.tvError);
        syncText = view.findViewById(R.id.sync_text);

        view.findViewById(R.id.bUnsync).setOnClickListener(v -> unsync());

        bSubmit.setOnClickListener(v -> {
            final String code = et.getText().toString();
            if (code.length() > 0)
                syncWithCode(code);
            else showError(requireActivity().getString(R.string.sync_code_required));
        });


        final boolean isSynchronized = UserController.isSynchronized(requireActivity());
        if (isSynchronized)
            showUnsyncLayout("");
        else showSyncLayout();

        return view;
    }

    private void syncWithCode(final String invitationCode) {
        try {
            final String uuid = UserController.getUUID(requireActivity());
            showLoading();
            Synchronize.request(invitationCode, uuid, new Synchronize.SynchronizeCallback() {
                @Override
                public void onSynchronize(final String sync_code) {
                    UserController.setSyncCode(requireActivity(), sync_code);
                    UserController.setUseCloud(requireActivity(), true);
                    Controller.loadDataBase(requireActivity(), new Controller.LoadCallback() {
                        @Override
                        public void onSuccess() {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    showUnsyncLayout(requireActivity().getString(R.string.sync_synchronized));
                                });
                            }
                        }

                        @Override
                        public void onError(String error, int errorCode) {
                            if (isAdded())
                                requireActivity().runOnUiThread(() -> showError(error));
                        }

                        @Override
                        public void onNetworkError() {
                            if (isAdded())
                                requireActivity().runOnUiThread(() -> showError(requireActivity().getString(R.string.network_error)));
                        }
                    });
                }

                @Override
                public void onNotFound() {
                    if (isAdded())
                        requireActivity().runOnUiThread(() -> showError(requireActivity().getString(R.string.synchronize_pair_not_found)));
                }

                @Override
                public void onNetworkError() {
                    if (isAdded())
                        requireActivity().runOnUiThread(() -> showError(requireActivity().getString(R.string.network_error)));
                }
            });
        } catch (Exception e) { showError(e.toString()); }
    }

    private void unsync() {
        Desynchronize.request(requireActivity(), new Desynchronize.DesynchronizeCallback() {
            @Override
            public void onDesynchronize(boolean ok) {

            }

            @Override
            public void onNetworkError() {

            }
        });
        UserController.unsync(requireActivity());
        showSyncLayout();
    }

    private void showUnsyncLayout(final String text) {
        unsynchronize_layout.setVisibility(View.VISIBLE);
        bSubmit.setVisibility(View.VISIBLE);
        synchronize_layout.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        syncText.setText(text);
        syncText.setVisibility((text.length() > 0) ? View.VISIBLE : View.GONE);
    }

    private void showSyncLayout() {
        synchronize_layout.setVisibility(View.VISIBLE);
        unsynchronize_layout.setVisibility(View.GONE);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        bSubmit.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
    }

    private void showError(final String text) {
        errorText.setText(text);
        errorText.setVisibility(View.VISIBLE);
        bSubmit.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

}
