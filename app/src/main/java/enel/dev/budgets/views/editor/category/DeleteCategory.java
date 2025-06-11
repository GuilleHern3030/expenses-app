package enel.dev.budgets.views.editor.category;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.category.Category;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeleteCategory#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeleteCategory extends CategoryEditorContext {

    public DeleteCategory() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param category a {@link Category} object
     * @return A new instance of fragment DeleteCategory.
     */
    // TODO: Rename and change types and number of parameters
    public static DeleteCategory newInstance(final Category category, final int index) {
        DeleteCategory fragment = new DeleteCategory();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, category.getName());
        args.putInt(ARG_COLOR, category.getColorId());
        args.putInt(ARG_ICON, category.getImageId());
        args.putBoolean(ARG_ISANINCOME, category.isAnIncome());
        args.putInt(ARG_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater parentInflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = parentInflater.inflate(R.layout.fragment_category_delete, container, false);

        final Category category = new Category(categoryName, categoryIcon, categoryColor, isAnIncome);

        view.findViewById(R.id.bCancel).setOnClickListener(v -> goBack());
        view.findViewById(R.id.bAccept).setOnClickListener(v -> resultCategoryDelete(category));

        FrameLayout color = view.findViewById(R.id.category_color);
        ImageView icon = view.findViewById(R.id.category_icon);
        TextView name = view.findViewById(R.id.category_name);

        color.setBackgroundResource(category.getColor());
        icon.setImageResource(category.getImage());
        name.setText(category.getName());

        return view;
    }
}