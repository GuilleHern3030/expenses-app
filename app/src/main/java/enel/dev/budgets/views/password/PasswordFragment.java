package enel.dev.budgets.views.password;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import enel.dev.budgets.R;
import enel.dev.budgets.utils.SnackBar;
import enel.dev.budgets.views.Fragment;
import enel.dev.budgets.views.home.HomeFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PasswordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PasswordFragment extends Fragment {

    public PasswordFragment() {
        // Required empty public constructor
    }

    private String password;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param passwordRequired Text required to close this fragment.
     * @return A new instance of fragment PasswordFragment.
     */
    public static PasswordFragment newInstance(final String passwordRequired) {
        PasswordFragment fragment = new PasswordFragment();
        Bundle args = new Bundle();
        args.putString("password", passwordRequired);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.password = getArguments().getString("password", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.password.isEmpty()) closeFragment();
        EditText etPassword = view.findViewById(R.id.etPassword);
        view.findViewById(R.id.bAccept).setOnClickListener(v -> {
            final boolean success = sendPassword(etPassword.getText().toString());
            if (success) closeFragment();
            else SnackBar.show(requireActivity(), view, requireActivity().getString(R.string.wrong_password));
        });
    }

    @Override
    public void onActionPressed() {

    }

    @Override
    public void onBackPressed() {
        requireActivity().finishAffinity();
    }

    private boolean sendPassword(final String psw) {
        return psw.equals(password);
    }

    private void closeFragment() {
        replaceFragment(HomeFragment.newInstance());
    }
}