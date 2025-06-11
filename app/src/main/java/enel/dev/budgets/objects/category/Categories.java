package enel.dev.budgets.objects.category;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import enel.dev.budgets.R;

public class Categories extends ArrayList<Category> {

    public Category getCategory(final String categoryName) {
        if (categoryName != null) {
            for (int i = 0; i < this.size(); i++) try {
                if (this.get(i).getName().equals(categoryName))
                    return this.get(i);
            } catch(Exception ignored) { }
        }
        return null;
    }

    public Categories incomes() {
        Categories categories = new Categories();
        for(int i = 0; i < this.size(); i++) {
            if (this.get(i).isAnIncome())
                categories.add(this.get(i).clone());
        }
        return categories;
    }

    public Categories expenses() {
        Categories categories = new Categories();
        for(int i = 0; i < this.size(); i++) {
            if (!this.get(i).isAnIncome())
                categories.add(this.get(i).clone());
        }
        return categories;
    }

    public int getIndex(final String categoryName) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getName().equals(categoryName))
                return i;
        }
        return -1;
    }

    public boolean exists(final String categoryName) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getName().equals(categoryName))
                return true;
        }
        return false;
    }

    public boolean remove(final String categoryName) {
        final int index = getIndex(categoryName);
        if (index >= 0)
            return this.remove(index) != null;
        return false;
    }

    public static Categories defaultList(@NonNull Context context) {
        Categories categories = new Categories();
        categories.add(new Category(context.getString(R.string.category_default_0), 76, 20, true));
        categories.add(new Category(context.getString(R.string.category_default_1), 94, 23, true));
        categories.add(new Category(context.getString(R.string.category_default_2), 78, 9, true));
        categories.add(new Category(context.getString(R.string.category_default_3), 14, 29));
        categories.add(new Category(context.getString(R.string.category_default_4), 19, 21));
        categories.add(new Category(context.getString(R.string.category_default_5), 13, 22));
        categories.add(new Category(context.getString(R.string.category_default_6), 43, 7));
        categories.add(new Category(context.getString(R.string.category_default_7), 52, 10));
        categories.add(new Category(context.getString(R.string.category_default_8), 51, 6));
        categories.add(new Category(context.getString(R.string.category_default_9), 17, 15));
        categories.add(new Category(context.getString(R.string.category_default_10), 47, 11));
        categories.add(new Category(context.getString(R.string.category_default_11), 33, 24));
        categories.add(new Category(context.getString(R.string.category_default_12), 111, 16));
        categories.add(new Category(context.getString(R.string.category_default_13), 106, 4));
        categories.add(new Category(context.getString(R.string.category_default_14), 27, 22));
        categories.add(new Category(context.getString(R.string.category_default_15), 66, 6));
        return categories;
    }

    public int getCategoryId(final Category category) {
        for (int i = 0; i < this.size(); i++) {
            if (category.getName().equals(this.get(i).getName()))
                return i;
        } return -1;
    }

    public Categories clone() {
        Categories clonedCategories = new Categories();
        for (Category category : this)
            clonedCategories.add(category.clone());
        return clonedCategories;
    }

}
