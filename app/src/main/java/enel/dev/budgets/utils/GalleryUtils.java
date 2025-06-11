package enel.dev.budgets.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;


public class GalleryUtils extends Fragment {

    private ActivityResultLauncher<Intent> galleryLauncher;
    public GalleryUtils(Fragment fragment) {



        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        this.mListener.onImageSelected(selectedImage);
                    }
                }
        );
    }

    public void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pickPhotoIntent.addCategory(Intent.CATEGORY_OPENABLE);
        pickPhotoIntent.setType("image/*");
        galleryLauncher.launch(pickPhotoIntent);
    }

    public void setOnFragmentInteractionListener(OnGalleryInteractionListener listener) {
        this.mListener = listener;
    }

    private OnGalleryInteractionListener mListener;
    public interface OnGalleryInteractionListener {
        void onImageSelected(Uri uri);
    }

}
