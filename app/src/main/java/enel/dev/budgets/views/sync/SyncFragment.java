package enel.dev.budgets.views.sync;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.UserController;
import enel.dev.budgets.views.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SyncFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SyncFragment extends Fragment {

    View view;

    private View signedLayout;
    private View unsignedLayout;
    private View loadingLayout;
    private View errorLayout;
    private TextView errorLayoutText;

    public SyncFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment.
     */
    public static SyncFragment newInstance(String... params) {
        SyncFragment fragment = new SyncFragment();
        fragment.setArguments(bundle(params));
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sync, container, false);

        fragmentAbove = view.findViewById(R.id.fragment_above);

        signedLayout = view.findViewById(R.id.signed_layout);
        unsignedLayout = view.findViewById(R.id.unsigned_layout);
        loadingLayout = view.findViewById(R.id.loading_layout);
        errorLayout = view.findViewById(R.id.error_layout);
        errorLayoutText = view.findViewById(R.id.error_layout_text);

        showSignLayout(UserController.isSignedIn(requireActivity()));

        view.findViewById(R.id.bSignIn).setOnClickListener(v -> signIn());
        view.findViewById(R.id.bSignOut).setOnClickListener(v -> signOut());
        view.findViewById(R.id.bPremium).setOnClickListener(v -> showFragmentAbove(new PremiumFragment()));
        view.findViewById(R.id.bPutCode).setOnClickListener(v -> showFragmentAbove(new SynchronizeFragment()));

        return view;
    }

    private void signIn() {

        Toast.makeText(requireActivity(), requireActivity().getString(R.string.commingson), Toast.LENGTH_SHORT).show();

        /*
        showLoading();
        UserController.signIn(requireActivity(), new UserController.SignCallback() {
            @Override
            public void onSuccess(SignIn.AuthResponse user) {
                if (isAdded()) {  // isAdded es true sólo si la vista (fragment) sigue activa
                    requireActivity().runOnUiThread(() -> showSignLayout(true));
                    // La información ya se guardó en la Base de Datos
                }
            }

            @Override
            public void onError(final Exception e) {
                if (isAdded()) { // isAdded es true sólo si la vista (fragment) sigue activa
                    if (e != null) {
                        requireActivity().runOnUiThread(() -> {
                            showSignLayout(false);
                            showError(e.toString());
                        });
                    } else { // Network error
                        requireActivity().runOnUiThread(() -> {
                            showSignLayout(false);
                            showError(requireActivity().getString(R.string.network_error));
                        });
                    }
                }
            }
        });*/
    }

    private void signOut() {
        hideError();
        UserController.signOut(requireActivity());
        showSignLayout(false);
    }

    private void showSignLayout(final boolean signed) {
        if (signed) {
            signedLayout.setVisibility(View.VISIBLE);
            unsignedLayout.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.GONE);
        } else {
            unsignedLayout.setVisibility(View.VISIBLE);
            signedLayout.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.GONE);
        }
    }

    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        unsignedLayout.setVisibility(View.GONE);
        signedLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

    private void showError(final String text) {
        errorLayout.setVisibility(View.VISIBLE);
        errorLayoutText.setText(text);
    }

    private void hideError() {
        errorLayout.setVisibility(View.GONE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActionPressed() { // Botón de sincronizar
        if (UserController.isSynchronized(requireActivity())) {
            showFragmentAbove(new PullFragment());
        } else Toast.makeText(requireActivity(), requireActivity().getString(R.string.synchronize_code_not_found), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        goHome();
    }

    private void showFragmentAbove(SyncFragmentContext fragment) {
        hideError();
        fragment.setOnFragmentInteractionListener(this::hideFragmentAbove);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

}