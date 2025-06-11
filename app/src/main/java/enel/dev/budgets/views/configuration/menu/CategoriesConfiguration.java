package enel.dev.budgets.views.configuration.menu;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.utils.CustomLinearLayoutManager;
import enel.dev.budgets.utils.RecyclerViewNoScrollable;
import enel.dev.budgets.views.configuration.ConfigurationContext;
import enel.dev.budgets.views.editor.category.CategoriesRecyclerView;
import enel.dev.budgets.views.editor.category.CreateCategory;
import enel.dev.budgets.views.editor.category.EditCategory;
import enel.dev.budgets.views.editor.category.CategoryEditorContext;
import enel.dev.budgets.views.editor.category.DeleteCategory;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class CategoriesConfiguration extends ConfigurationContext implements CategoryEditorContext.OnCategoryInteractionListener {

    private boolean incomes = false;
    private TextView textView;
    private FrameLayout categoryEditorContainer;
    private RecyclerViewNoScrollable recyclerView;
    private CategoriesRecyclerView categoryList;

    public static CategoriesConfiguration newInstance(final boolean isAnIncome) {
        CategoriesConfiguration fragment = new CategoriesConfiguration();
        Bundle args = new Bundle();
        args.putBoolean("isanincome", isAnIncome);
        fragment.setArguments(args);
        return fragment;
    }

    public CategoriesConfiguration() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            this.incomes = getArguments().getBoolean("isanincome", false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_configuration_categories, container, false);

        view.findViewById(R.id.bBack).setOnClickListener(v -> back());

        categoryEditorContainer = view.findViewById(R.id.category_frame);

        TextView title = view.findViewById(R.id.app_name);
        title.setText(incomes ?
                requireActivity().getString(R.string.config_category_incomes):
                requireActivity().getString(R.string.config_category_expenses));

        TextView bAddText = view.findViewById(R.id.add_new_category_text);
        bAddText.setTextColor(incomes ?
                requireActivity().getColor(R.color.income_text):
                requireActivity().getColor(R.color.expense_text));

        LinearLayout bAdd = view.findViewById(R.id.add_new_category_frame);
        bAdd.setOnClickListener(v -> showFragmentAbove(CreateCategory.newInstance(incomes)));

        this.textView = view.findViewById(R.id.categories_list_empty);
        recyclerView = view.findViewById(R.id.categories_list);
        showCategories();

        return view;
    }

    private void showCategories() {

        Categories categories = incomes ? Controller.categories(requireActivity()).get().incomes() : Controller.categories(requireActivity()).get().expenses();

        recyclerView.setLayoutManager(new CustomLinearLayoutManager(getContext()));
        categoryList = new CategoriesRecyclerView(categories);
        recyclerView.setAdapter(categoryList);

        // Listener
        categoryList.setOnCategoryCLick(new CategoriesRecyclerView.onCategoryClickListener() {
            @Override
            public void onCategoryClicked(int row, Category category) {
                showFragmentAbove(EditCategory.newInstance(category, row));
            }

            @Override
            public boolean onCategoryLongClicked(int row, Category category) {
                showFragmentAbove(DeleteCategory.newInstance(category, row));
                return true;
            }
        });

        if (categories.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private void showFragmentAbove(CategoryEditorContext fragment) {
        fragment.setOnFragmentInteractionListener(this);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.category_frame, fragment);
        fragmentTransaction.commit();
        categoryEditorContainer.setVisibility(View.VISIBLE);
    }

    private void hideFragmentAbove() {
        categoryEditorContainer.setVisibility(View.GONE);
    }

    @Override
    public void onCancelOperation() {
        hideFragmentAbove();
    }

    @Override
    public void onCategoryCreated(Category category) {
        hideFragmentAbove();
        categoryList.add(category);
    }

    @Override
    public void onCategoryDeleted(Category category) {
        hideFragmentAbove();
        categoryList.remove(category);
    }

    @Override
    public void onCategoryEdited(Category oldCategory, Category newCategory) {
        hideFragmentAbove();
        categoryList.set(oldCategory, newCategory);
    }
}