package enel.dev.budgets.views.editor.transaction;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import enel.dev.budgets.R;

public class EditTextLayout extends Fragment {

    public EditTextLayout() {
        // Required empty public constructor
    }

    private String initDescription;

    public static EditTextLayout newInstance(final String description) {
        EditTextLayout fragment = new EditTextLayout();
        Bundle args = new Bundle();
        args.putString("initdescription", description);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initDescription = getArguments().getString("initdescription");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_description, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText description = view.findViewById(R.id.etDescription);

        try { // show keyboard
            description.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(description, InputMethodManager.SHOW_IMPLICIT);
        } catch(Exception ignored) { }

        description.setText(initDescription);

        view.findViewById(R.id.bCancel).setOnClickListener(v -> {
            hideKeyboard(view);
            listener.onCancelEdition();
        });

        view.findViewById(R.id.bAccept).setOnClickListener(v -> {
            hideKeyboard(view);
            String text = description.getText().toString().replaceAll(";", ",");
            listener.onEditDescription(text);
        });
    }

    private OnDescriptionChangeListener listener;
    public interface OnDescriptionChangeListener {
        void onEditDescription(final String description);
        void onCancelEdition();
    }

    public void setOnDescriptionChangeListener(OnDescriptionChangeListener listener) {
        this.listener = listener;
    }

    private void hideKeyboard(final View view) {
        try { // hide keyboard
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception ignored) { }
    }
}