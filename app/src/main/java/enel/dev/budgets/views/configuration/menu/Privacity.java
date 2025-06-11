package enel.dev.budgets.views.configuration.menu;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import enel.dev.budgets.R;
import enel.dev.budgets.views.configuration.ConfigurationContext;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class Privacity extends ConfigurationContext {

    public Privacity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_privacity, container, false);

        view.findViewById(R.id.bBack).setOnClickListener(v -> back());

        return view;
    }
}