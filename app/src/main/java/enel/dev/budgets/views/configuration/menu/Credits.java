package enel.dev.budgets.views.configuration.menu;

import android.content.Intent;
import android.net.Uri;
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
public class Credits extends ConfigurationContext {

    public Credits() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_credits, container, false);

        final String url = requireActivity().getString(R.string.web_page_link);

        view.findViewById(R.id.bBack).setOnClickListener(v -> back());

        view.findViewById(R.id.bWebPage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        return view;
    }
}