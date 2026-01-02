package enel.dev.budgets.views.sync;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import enel.dev.budgets.R;

public class PremiumFragment extends SyncFragmentContext {

    public PremiumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_premium, container, false);

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
