package enel.dev.budgets.views.editor.transaction;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import enel.dev.budgets.R;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.utils.PhotoManager;
import enel.dev.budgets.utils.ZoomableImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransactionImage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionImage extends Fragment {

    private String photoUri;
    private String fileName;

    public TransactionImage() {
        // Required empty public constructor
    }

    public static TransactionImage newInstance(final Transaction transaction) {
        TransactionImage fragment = new TransactionImage();
        Bundle args = new Bundle();
        args.putString("uri", transaction.getPhotoUri());
        args.putString("filename", transaction.getPhotoFileName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {
            fileName = getArguments().getString("filename");
            photoUri = getArguments().getString("uri");
        } catch(Exception ignored) {
            photoUri = null;
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transaction_image, container, false);
        view.findViewById(R.id.bBack).setOnClickListener(v -> closeFragment());

        ZoomableImageView imageView = view.findViewById(R.id.imageView);

        if (photoUri != null && photoUri.length() > 0)
            imageView.setImageBitmap(PhotoManager.getImage(photoUri));

        return view;
    }

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        this.mListener = listener;
    }

    private OnFragmentInteractionListener mListener;
    public interface OnFragmentInteractionListener {
        void onCloseFragment();
    }

    private void closeFragment() {
        mListener.onCloseFragment();
    }
    //</editor-fold>
}