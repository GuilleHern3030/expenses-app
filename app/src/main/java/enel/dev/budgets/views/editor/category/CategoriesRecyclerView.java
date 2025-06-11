package enel.dev.budgets.views.editor.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;

public class CategoriesRecyclerView extends RecyclerView.Adapter<CategoriesRecyclerView.ViewHolder> {
    private final Categories mData;

    public CategoriesRecyclerView(Categories data) {
        this.mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_categories, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int row) {
        final Category category = mData.get(row);

        holder.categoryBackground.setBackgroundResource(category.getColor());
        holder.categoryImage.setImageResource(category.getImage());
        holder.categoryName.setText(category.getName());

        holder.frame.setOnClickListener(view1 -> onItemClickListener.onCategoryClicked(row, category));
        holder.frame.setOnLongClickListener(view1 -> onItemClickListener.onCategoryLongClicked(row, category));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    // Agregar un nuevo ítem al final de la lista
    public void add(Category category) {
        if (!mData.exists(category.getName())) {
            int position = mData.size();
            mData.add(category);
            notifyItemInserted(position);
        }
    }

    public void set(final Category oldCategory, final Category category) {
        final int index = mData.getIndex(oldCategory.getName());
        if (index >= 0 && index < mData.size()) {
            mData.set(index, category);
            notifyItemChanged(index);
        }
    }

    // Eliminar un ítem en una posición específica
    public void remove(Category category) {
        final int index = mData.getIndex(category.getName());
        if (index >= 0 && index < mData.size()) {
            mData.remove(index);
            notifyItemRemoved(index);
        }
    }

    // Cargar los view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout frame;
        ImageView categoryImage;
        FrameLayout categoryBackground;
        TextView categoryName;

        public ViewHolder(View view) {
            super(view);
            frame = view.findViewById(R.id.frame);
            categoryImage = view.findViewById(R.id.category_image);
            categoryBackground = view.findViewById(R.id.category_image_container);
            categoryName = view.findViewById(R.id.category_name);
        }
    }

    // Listener
    private onCategoryClickListener onItemClickListener;
    public interface onCategoryClickListener {
        void onCategoryClicked(final int row, final Category category);
        boolean onCategoryLongClicked(final int row, final Category category);
    }
    public void setOnCategoryCLick(onCategoryClickListener listener) {
        this.onItemClickListener = listener;
    } // */
}
