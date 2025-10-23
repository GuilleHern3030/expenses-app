package enel.dev.budgets.views.configuration;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import enel.dev.budgets.views.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import enel.dev.budgets.R;
import enel.dev.budgets.views.configuration.menu.CategoriesConfiguration;
import enel.dev.budgets.views.configuration.menu.CoinConfiguration;
import enel.dev.budgets.views.configuration.menu.Credits;
import enel.dev.budgets.views.configuration.menu.DecimalConfiguration;
import enel.dev.budgets.views.configuration.menu.PasswordConfiguration;
import enel.dev.budgets.views.configuration.menu.Privacity;
import enel.dev.budgets.views.configuration.menu.RemoveConfiguration;
import enel.dev.budgets.views.configuration.menu.WideConfiguration;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigurationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigurationFragment extends Fragment {

    View view;

    public ConfigurationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment.
     */
    public static ConfigurationFragment newInstance(String... params) {
        ConfigurationFragment fragment = new ConfigurationFragment();
        fragment.setArguments(bundle(params));
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_configuration, container, false);

        fragmentAbove = view.findViewById(R.id.fragment_above);

        LinearLayout configCoin = view.findViewById(R.id.bConfigCoin);
        configCoin.setOnClickListener(v -> showFragmentAbove(new CoinConfiguration()));

        LinearLayout configDecimal = view.findViewById(R.id.bConfigDecimal);
        configDecimal.setOnClickListener(v -> showFragmentAbove(new DecimalConfiguration()));

        LinearLayout configExpensesCategories = view.findViewById(R.id.bConfigExpenses);
        configExpensesCategories.setOnClickListener(v -> showFragmentAbove(CategoriesConfiguration.newInstance(false)));

        LinearLayout configIncomesCategories = view.findViewById(R.id.bConfigIncomes);
        configIncomesCategories.setOnClickListener(v -> showFragmentAbove(CategoriesConfiguration.newInstance(true)));

        LinearLayout configPassword = view.findViewById(R.id.bConfigPassword);
        configPassword.setOnClickListener(v -> showFragmentAbove(new PasswordConfiguration()));

        LinearLayout configPrivacy = view.findViewById(R.id.bConfigPrivacity);
        configPrivacy.setOnClickListener(v -> showFragmentAbove(new Privacity()));

        LinearLayout configRate = view.findViewById(R.id.bConfigRate);
        configRate.setOnClickListener(v -> Toast.makeText(requireActivity(), "Rate us <3", Toast.LENGTH_SHORT).show());

        LinearLayout configDonation = view.findViewById(R.id.bConfigDonation);
        configDonation.setOnClickListener(v -> Toast.makeText(requireActivity(), "Please donate <3", Toast.LENGTH_SHORT).show());

        LinearLayout configCredits = view.findViewById(R.id.bConfigCredits);
        configCredits.setOnClickListener(v -> showFragmentAbove(new Credits()));

        LinearLayout configWide = view.findViewById(R.id.bConfigWide);
        configWide.setOnClickListener(v -> showFragmentAbove(new WideConfiguration()));

        LinearLayout configDelete = view.findViewById(R.id.bConfigDelete);
        configDelete.setOnClickListener(v -> showFragmentAbove(new RemoveConfiguration()));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActionPressed() {
        ReviewManager manager = ReviewManagerFactory.create(requireContext());
        Task<ReviewInfo> request = manager.requestReviewFlow();

        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(requireActivity(), reviewInfo);
                flow.addOnCompleteListener(task2 -> {
                    // El flujo de reseña terminó (no sabés si el usuario realmente calificó)
                });
            } else try {
                // Abre la app de Play Store
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + requireContext().getPackageName())));
            } catch (android.content.ActivityNotFoundException e) {
                // Si no tiene Play Store, abre en el navegador
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().getPackageName())));
            }
        });
    }

    @Override
    public void onBackPressed() {
        goHome();
    }

    private void showFragmentAbove(ConfigurationContext fragment) {
        fragment.setOnFragmentInteractionListener(this::hideFragmentAbove);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_above, fragment);
        fragmentTransaction.commit();
        fragmentAbove.setVisibility(View.VISIBLE);
    }

}