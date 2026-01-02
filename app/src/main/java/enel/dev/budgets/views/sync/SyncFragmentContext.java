package enel.dev.budgets.views.sync;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class SyncFragmentContext extends Fragment {

    public SyncFragmentContext() {
        // Empty constructor required
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Registra un callback para el botón "Atrás"
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        closeFragment();
                    }
                });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void back() {
        closeFragment();
    }

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setOnFragmentInteractionListener(enel.dev.budgets.views.configuration.ConfigurationContext.OnFragmentInteractionListener listener) {
        this.mListener = listener;
    }

    private enel.dev.budgets.views.configuration.ConfigurationContext.OnFragmentInteractionListener mListener;
    public interface OnFragmentInteractionListener {
        void onFragmentClosed();
    }

    protected void closeFragment() {
        if (mListener != null) mListener.onFragmentClosed();
        getParentFragmentManager().beginTransaction().remove(this).commit(); // Remove this fragment
        //closeFragment();
    }
    //</editor-fold>
}
