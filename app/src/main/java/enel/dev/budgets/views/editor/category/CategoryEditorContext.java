package enel.dev.budgets.views.editor.category;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.utils.SnackBar;

public abstract class CategoryEditorContext extends Fragment {

    public CategoryEditorContext() {

    }

    protected static final String ARG_NAME = "name";
    protected static final String ARG_COLOR = "color";
    protected static final String ARG_ICON = "icon";
    protected static final String ARG_ISANINCOME = "isanincome";
    protected static final String ARG_INDEX = "index";

    protected Categories categories;

    protected int index;

    private boolean fetching = false;

    // Category attributes
    protected Category oldCategory; // isAnIncome
    protected String categoryName;
    protected int categoryColor;
    protected int categoryIcon;
    protected boolean isAnIncome; // isAnIncome

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categories = Controller.categories(requireActivity()).get();

        if (getArguments() != null) try {
            this.categoryName = getArguments().getString(ARG_NAME, "");
            this.categoryColor = getArguments().getInt(ARG_COLOR, -1);
            this.categoryIcon = getArguments().getInt(ARG_ICON, -1);
            this.isAnIncome = getArguments().getBoolean(ARG_ISANINCOME, false);
            this.index = getArguments().getInt(ARG_INDEX, -1);
            this.oldCategory = new Category(categoryName, categoryIcon, categoryColor, isAnIncome);
        } catch(Exception ignored) {  }
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
                        goBack();
                    }
                });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.subListener = null;
    }

    public void setOnFragmentInteractionListener(OnCategoryInteractionListener listener) {
        this.subListener = listener;
    }

    private OnCategoryInteractionListener subListener;
    public interface OnCategoryInteractionListener {

        void onCancelOperation();
        void onCategoryCreated(final Category category);
        void onCategoryDeleted(final Category category);
        void onCategoryEdited(final Category oldCategory, final Category category);
    }

    protected void closeFragment() {
        goBack();
    }

    protected void goBack() {
        if (subListener != null) subListener.onCancelOperation();
        getParentFragmentManager().beginTransaction().remove(this).commit(); // Remove this fragment
    }

    protected void resultCategoryCreate(final Category category) {
        if (!fetching) {
            fetching = true;

            final Fragment fragment = this;
            Controller.categories(requireActivity()).add(category, new Controller.SQLcallback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        fetching = false;
                        if (subListener != null) subListener.onCategoryCreated(category);
                        getParentFragmentManager().beginTransaction().remove(fragment).commit(); // Remove this fragment
                    }
                }

                @Override
                public void onError(String error) {
                    if (isAdded()) {
                        fetching = false;
                        SnackBar.show(requireActivity(), getView(), error);
                    }
                }

                @Override
                public void onNetworkError() {
                    if (isAdded()) {
                        fetching = false;
                        SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.network_error));
                    }
                }
            });
        }
    }

    protected void resultCategoryDelete(final Category category) {
        final Fragment fragment = this;
        Controller.categories(requireActivity()).delete(category, new Controller.SQLcallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    if (subListener != null) subListener.onCategoryDeleted(category);
                    getParentFragmentManager().beginTransaction().remove(fragment).commit(); // Remove this fragment
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded())
                    SnackBar.show(requireActivity(), getView(), error);
            }

            @Override
            public void onNetworkError() {
                if (isAdded())
                    SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.network_error));
            }
        });
    }

    protected void resultCategoryEdit(final Category oldCategory, final Category category) {
        if (!fetching) {
            fetching = true;
            final Fragment fragment = this;
            Controller.categories(requireActivity()).edit(oldCategory.getName(), category, new Controller.SQLcallback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        fetching = false;
                        if (subListener != null)
                            subListener.onCategoryEdited(oldCategory, category);
                        getParentFragmentManager().beginTransaction().remove(fragment).commit(); // Remove this fragment
                    }
                }

                @Override
                public void onError(String error) {
                    if (isAdded()) {
                        fetching = false;
                        SnackBar.show(requireActivity(), getView(), error);
                    }
                }

                @Override
                public void onNetworkError() {
                    if (isAdded()) {
                        fetching = false;
                        SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.network_error));
                    }
                }
            });
        }
    }
}
