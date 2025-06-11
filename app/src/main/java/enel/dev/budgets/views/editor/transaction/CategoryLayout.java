package enel.dev.budgets.views.editor.transaction;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DiffUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.utils.CustomLinearLayoutManager;
import enel.dev.budgets.utils.RecyclerViewNoScrollable;
import enel.dev.budgets.views.editor.category.CategoriesRecyclerView;
import enel.dev.budgets.views.editor.category.CategoryEditorContext;
import enel.dev.budgets.views.editor.category.CreateCategory;
import enel.dev.budgets.views.editor.category.EditCategory;

public class CategoryLayout extends Fragment implements CategoryEditorContext.OnCategoryInteractionListener {

    Categories categories;
    Category initCategory;
    private FrameLayout categoryEditorContainer;
    private TextView textView;
    private boolean incomes;


    private RecyclerViewNoScrollable recyclerView;
    private CategoriesRecyclerView categoryList;


    public CategoryLayout() {
        // Required empty public constructor
    }

    public static CategoryLayout newInstance(final Category category) {
        CategoryLayout fragment = new CategoryLayout();
        Bundle args = new Bundle();
        args.putString("categoryname", category.getName());
        args.putBoolean("incomes", category.isAnIncome());
        fragment.setArguments(args);
        return fragment;
    }

    public static CategoryLayout newInstance(final boolean isAnIncome) {
        CategoryLayout fragment = new CategoryLayout();
        Bundle args = new Bundle();
        args.putBoolean("incomes", isAnIncome);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categories = Controller.categories(requireActivity()).get();
        if (getArguments() != null) {
            final String categoryname = getArguments().getString("categoryname", "");
            this.incomes = getArguments().getBoolean("incomes");
            this.initCategory = (categoryname != null && categoryname.length() > 0) ?
                    categories.getCategory(categoryname) : null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Registra un callback para el botón "Atrás"
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        closeFragment();
                    }
                });

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryEditorContainer = view.findViewById(R.id.category_frame);

        view.findViewById(R.id.add_new_category_frame).setOnClickListener(v -> showFragmentAbove(CreateCategory.newInstance(incomes)));

        ImageView bCancel = view.findViewById(R.id.bCancel);
        bCancel.setOnClickListener(v -> closeFragment());

        this.textView = view.findViewById(R.id.categories_list_empty);
        recyclerView = view.findViewById(R.id.categories_list);
        showCategories(categories);
    }

    private void showCategories(Categories categoriesList) {

        Categories categories = incomes ? categoriesList.incomes() : categoriesList.expenses();

        recyclerView.setLayoutManager(new CustomLinearLayoutManager(getContext()));
        categoryList = new CategoriesRecyclerView(categories);
        recyclerView.setAdapter(categoryList);

        categoryList.setOnCategoryCLick(new CategoriesRecyclerView.onCategoryClickListener() {
            @Override
            public void onCategoryClicked(int row, Category category) {
                selectCategory(category);
            }

            @Override
            public boolean onCategoryLongClicked(int row, Category category) {
                showFragmentAbove(EditCategory.newInstance(category, row));
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

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    private OnTransactionCategoryListener listener;
    public interface OnTransactionCategoryListener {
        void onCategorySelected(final Category category);
        void onCancelSelection();
    }

    public void setOnTransactionCategoryListener(OnTransactionCategoryListener listener) {
        this.listener = listener;
    }

    private void closeFragment() {
        this.listener.onCancelSelection();
    }

    private void selectCategory(final Category category) {
        this.listener.onCategorySelected(category);
    }
    //</editor-fold>
}