package enel.dev.budgets.views.editor.transaction;

import android.Manifest;
import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.utils.PhotoManager;
import enel.dev.budgets.utils.ZoomableImageView;

public class CameraLayout extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Uri photoURIObtained;
    private ZoomableImageView imageView;

    public CameraLayout() {
        // Required empty public constructor
    }

    private String fileName;
    private String photoUri;
    private int id;
    private Date date;

    public static CameraLayout newInstance(final Transaction transaction) {
        Bundle args = new Bundle();
        args.putString("uri", transaction.getPhotoUri());
        args.putString("filename", transaction.getPhotoFileName());
        args.putString("date", transaction.getDate().toString());
        args.putInt("id", transaction.id());
        CameraLayout fragment = new CameraLayout();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {
            this.id = getArguments().getInt("id", -1);
            this.photoUri = getArguments().getString("uri", "");
            this.date = new Date(getArguments().getString("date", ""));
            fileName = getArguments().getString("filename");
        } catch (Exception ignored) { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transaction_camera, container, false);

        view.findViewById(R.id.bCancel).setOnClickListener(v -> {
            photoURIObtained = null;
            photoUri = null;
            listener.onPhotoChanged(photoUri);
        });
        view.findViewById(R.id.bBack).setOnClickListener(v -> listener.onCancelOperation());

        view.findViewById(R.id.bAccept).setOnClickListener(v -> {
            try {
                final String newPath = PhotoManager.saveImageToAppDirectory(requireActivity(), photoURIObtained, fileName);
                listener.onPhotoChanged(newPath);
            } catch (Exception e) {
                photoURIObtained = null;
                listener.onPhotoChanged(photoUri);
            }
        });

        imageView = view.findViewById(R.id.imageView);

        if (photoUri != null && photoUri.length() > 0) {
            imageView.setImageBitmap(PhotoManager.getImage(photoUri));
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonTakePhoto = view.findViewById(R.id.button_take_photo);
        Button buttonChoosePhoto = view.findViewById(R.id.button_choose_photo);

        // Configurar los launchers
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // Manejar la imagen capturada
                            if (photoURIObtained != null) {
                                imageView.setImageURI(photoURIObtained); // Usa la photoUri para obtener la imagen
                            } else {
                                Toast.makeText(requireActivity(), "photouri null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        photoURIObtained = selectedImage;
                        imageView.setImageURI(selectedImage);
                    }
                }
        );

        buttonTakePhoto.setOnClickListener(v -> {
            checkPermissionsAndOpenCamera();
        });

        buttonChoosePhoto.setOnClickListener(v -> {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pickPhotoIntent.addCategory(Intent.CATEGORY_OPENABLE);
            pickPhotoIntent.setType("image/*");
            galleryLauncher.launch(pickPhotoIntent);
        });
    }

    private void checkPermissionsAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
            photoURIObtained = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURIObtained);
            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireActivity(), "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    private OnTransactionPhotoListener listener;
    public interface OnTransactionPhotoListener {
        void onPhotoChanged(final String newPhotoUri);
        void onCancelOperation();
    }

    public void setOnTransactionPhotoListener(OnTransactionPhotoListener listener) {
        this.listener = listener;
    }
    //</editor-fold>

}