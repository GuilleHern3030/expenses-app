package enel.dev.budgets.views.configuration.menu;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.utils.SnackBar;
import enel.dev.budgets.views.configuration.ConfigurationContext;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class PasswordConfiguration extends ConfigurationContext {

    CardView creationFrame;
    CardView deleteFrame;
    EditText etPassword;

    public PasswordConfiguration() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_configuration_password, container, false);

        creationFrame = view.findViewById(R.id.password_creation_frame);
        deleteFrame = view.findViewById(R.id.password_delete_frame);
        etPassword = view.findViewById(R.id.etPassword);

        view.findViewById(R.id.bBack).setOnClickListener(v -> back());
        view.findViewById(R.id.bCancel).setOnClickListener(v -> back());
        view.findViewById(R.id.bAccept).setOnClickListener(v -> validatePassword());
        view.findViewById(R.id.bDelete).setOnClickListener(v -> deletePassword());

        if (Preferences.password(requireActivity()).isEmpty())
            showCreatePasswordBox();
        else
            showDeletePasswordBox();

        return view;
    }

    private void showCreatePasswordBox() {
        creationFrame.setVisibility(View.VISIBLE);
        deleteFrame.setVisibility(View.GONE);
    }

    private void showDeletePasswordBox() {
        creationFrame.setVisibility(View.GONE);
        deleteFrame.setVisibility(View.VISIBLE);
    }

    private void validatePassword() {
        final String password = etPassword.getText().toString();
        if (!password.isEmpty()) {
            Preferences.setPassword(requireActivity(), password);
            showDeletePasswordBox();
            SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.password_created));
            return;
        }
        SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.password_created_failed));
    }

    private void deletePassword() {
        showCreatePasswordBox();
        SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.password_deleted));
        Preferences.setPassword(requireActivity(), "");
    }
}