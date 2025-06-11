package enel.dev.budgets.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import enel.dev.budgets.R;

public class SnackBar {

    public static void show(final Context context, final View view, final String text) {
        Snackbar snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG);

        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.snackbar, null);

        TextView textView = customView.findViewById(R.id.snackbar_text);
        textView.setText(text);

        snackbarLayout.addView(customView, 0);
        snackbar.show();
    }

}
