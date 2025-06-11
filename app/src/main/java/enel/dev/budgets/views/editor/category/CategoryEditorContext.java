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
        boolean success = Controller.categories(requireActivity()).add(category);
        if (success) {
            if (subListener != null) subListener.onCategoryCreated(category);
            getParentFragmentManager().beginTransaction().remove(this).commit(); // Remove this fragment
        } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.category_operation_failed));
    }

    protected void resultCategoryDelete(final Category category) {
        boolean success = Controller.categories(requireActivity()).delete(category);
        if (success) {
            if (subListener != null) subListener.onCategoryDeleted(category);
            getParentFragmentManager().beginTransaction().remove(this).commit(); // Remove this fragment
        } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.category_operation_failed));
    }

    protected void resultCategoryEdit(final Category oldCategory, final Category category) {
        boolean success = Controller.categories(requireActivity()).edit(oldCategory.getName(), category);
        if (success) {
            if (subListener != null) subListener.onCategoryEdited(oldCategory, category);
            getParentFragmentManager().beginTransaction().remove(this).commit(); // Remove this fragment
        } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.category_operation_failed));
    }
}
