package enel.dev.budgets;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import enel.dev.budgets.data.preferences.Preferences;

import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.views.Fragment;
import enel.dev.budgets.views.home.HomeFragment;
import enel.dev.budgets.views.password.PasswordFragment;

public class MainActivity extends AppCompatActivity implements Fragment.OnChangeFragmentListener {

    private Fragment fragment;

    /**
     * This function is executed when the app is opened and when the screen orientation is changed
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadDefaultData();

        setWideMode(Preferences.wideMode(this));

        // Initial fragment
        final String password = Preferences.password(this);
        if (savedInstanceState == null) {
            if (password != null && password.length() > 0)
                showFragment(PasswordFragment.newInstance(password));
            else
                showFragment(HomeFragment.newInstance());
        }

    }

    /*@Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_FRAGMENT_ABOVE_SHOWED, fragmentAbove.getVisibility() == View.VISIBLE); // Guardar el estado actual
        outState.putInt(KEY_ACTION_IMAGE, navigationActionImageRes); // Guardar el estado actual
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restaurar el estado guardado
        boolean fragmentAboveShowed = savedInstanceState.getBoolean(KEY_FRAGMENT_ABOVE_SHOWED);
        if (fragmentAboveShowed)
            fragmentAbove.setVisibility(View.VISIBLE);
        if (savedInstanceState.getInt(KEY_ACTION_IMAGE, -1) != -1) {
            navigationActionImageRes = savedInstanceState.getInt(KEY_ACTION_IMAGE);
            navigationActionImage.setImageResource(navigationActionImageRes);
        }
    }*/

    private void showFragment(final Fragment fragment) {
        this.fragment = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit();

        //fixNavButtonsEx(findViewById(R.id.main_activity));
    }

    private void loadDefaultData() {
        Preferences.loadDefaultData(this);
        Controller.loadDefaultData(this);
    }

    @Override
    public void onChangeFragment(Fragment newFragment) {
        showFragment(newFragment);
    }

    public void setWideMode(final boolean wideMode) {
        Log.d("NAVBAR", "Barra fix");

        View rootView = findViewById(R.id.main_activity);

        // Listener moderno con WindowInsetsCompat
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Log.d("NAVBAR", "Barra fix 1");

            // Altura de la barra de navegación inferior
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            // Ajuste superior (status bar)
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            rootView.setPadding(
                    rootView.getPaddingLeft(),
                    wideMode ? statusBarHeight + dpToPx(16) : statusBarHeight, // separacion extra
                    rootView.getPaddingRight(),
                    rootView.getPaddingBottom()
            );

            // Ajustamos padding inferior de los botones
            rootView.setPadding(
                    rootView.getPaddingLeft(),
                    rootView.getPaddingTop(),
                    rootView.getPaddingRight(),
                    wideMode ? navBarHeight + dpToPx(48) : navBarHeight // 16dp extra de margen
            );

            if(navBarHeight > 0){
                Log.d("NAVBAR", "Barra de navegación detectada. Altura: " + navBarHeight);
            } else {
                Log.d("NAVBAR", "Sin barra de navegación (gestos o botones físicos)");
            }

            // Retornamos el insets original para que otros views también lo reciban
            return insets;
        });

        rootView.post(() -> ViewCompat.requestApplyInsets(rootView));
    }

    private int dpToPx(int dp){
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}