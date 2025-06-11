package enel.dev.budgets.views.configuration.menu;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.views.configuration.ConfigurationContext;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class DecimalConfiguration extends ConfigurationContext {

    public DecimalConfiguration() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_configuration_decimal, container, false);

        view.findViewById(R.id.bBack).setOnClickListener(v -> back());

        try {

            NumberFormat defaultDecimalFormat = Preferences.decimalFormat(requireActivity());

            final CheckBox[] checkBoxes = new CheckBox[]{
                    view.findViewById(R.id.checkBox1),
                    view.findViewById(R.id.checkBox2),
                    view.findViewById(R.id.checkBox3),
                    view.findViewById(R.id.checkBox4),
                    view.findViewById(R.id.checkBox5),
                    view.findViewById(R.id.checkBox6),
                    view.findViewById(R.id.checkBox7)
            };

            checkBoxes[defaultDecimalFormat.ordinal()].setChecked(true);

            for (int c = 0; c < checkBoxes.length; c++) {
                final CheckBox cb = checkBoxes[c];
                final int ordinal = c;
                cb.setOnClickListener(v -> {
                    for (CheckBox _cb : checkBoxes) _cb.setChecked(false);
                    cb.setChecked(true);
                    Preferences.setDecimalFormat(requireActivity(), NumberFormat.newInstance(ordinal));
                });
            }

        } catch (Exception e) {
            Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }

        return view;
    }
}