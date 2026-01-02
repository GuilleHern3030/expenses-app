package enel.dev.budgets.views.configuration.menu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

import enel.dev.budgets.MainActivity;
import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.views.configuration.ConfigurationContext;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class WideConfiguration extends ConfigurationContext {

    public WideConfiguration() {
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_configuration_wide, container, false);

        view.findViewById(R.id.bBack).setOnClickListener(v -> back());

        Switch wideMode = view.findViewById(R.id.widemode);

        wideMode.setChecked(Preferences.wideMode(requireActivity()));

        wideMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Preferences.setMoreCoinsAvailable(requireActivity(), isChecked);
            MainActivity mainActivity = (MainActivity)requireActivity();
            mainActivity.setWideMode(isChecked);
        });

        return view;
    }

}