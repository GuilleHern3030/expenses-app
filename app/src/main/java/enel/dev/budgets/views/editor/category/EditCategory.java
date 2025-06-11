package enel.dev.budgets.views.editor.category;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.utils.SnackBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditCategory#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditCategory extends CategoryEditorContext {

    EditText etCategoryName;

    private String oldCategoryName;

    public EditCategory() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param category a #Category object
     * @return A new instance of fragment EditCategory.
     */
    // TODO: Rename and change types and number of parameters
    public static EditCategory newInstance(final Category category, final int index) { // category editor
        EditCategory fragment = new EditCategory();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categories = Controller.categories(requireActivity()).get();

        if (getArguments() != null) try {
            this.oldCategoryName = getArguments().getString(ARG_NAME, "");
        } catch(Exception ignored) {  }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category_edit, container, false);

        view.findViewById(R.id.bBack).setOnClickListener(v -> goBack());
        view.findViewById(R.id.bCancel).setOnClickListener(v -> goBack());
        view.findViewById(R.id.bAccept).setOnClickListener(v -> validateInputs());

        etCategoryName = view.findViewById(R.id.category_name);
        if (this.categoryName.length() > 0) etCategoryName.setText(this.categoryName);

        Spinner iconsSpinner = view.findViewById(R.id.spinner_categories_icons);
        Spinner colorsSpinner = view.findViewById(R.id.spinner_categories_colors);

        showIconsList(iconsSpinner, this.categoryIcon);
        showColorsList(colorsSpinner, this.categoryColor);

        return view;
    }

    private void validateInputs() {

        final String inputName = etCategoryName.getText().toString().replaceAll(";", "").replaceAll(",", "");

        if (inputName.length() > 0) {
            if (!categories.exists(inputName) || oldCategoryName.equals(inputName)) {
                resultCategoryEdit(oldCategory, new Category(inputName, this.categoryIcon, this.categoryColor, this.isAnIncome));
            } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.category_already_exists));
        } else SnackBar.show(requireActivity(), getView(), requireActivity().getString(R.string.category_no_name));
    }

    private void showIconsList(Spinner spinner, final int selection) {
        IconsSpinner adapter = new IconsSpinner(requireActivity());
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryIcon = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (selection >= 0)
            spinner.setSelection(selection);
    }

    private void showColorsList(Spinner spinner, final int selection) {
        ColorsSpinner adapter = new ColorsSpinner(requireActivity());
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryColor = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (selection >= 0)
            spinner.setSelection(selection);
    }


}