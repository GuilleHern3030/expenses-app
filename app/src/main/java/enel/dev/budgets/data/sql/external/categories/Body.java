package enel.dev.budgets.data.sql.external.categories;

import androidx.annotation.Nullable;

import enel.dev.budgets.objects.category.Category;

class Body {
    public static Body create(final Category category, @Nullable final String old_name) {
        return new Body(
                category.getName(),
                category.getImageId(),
                category.getColorId(),
                category.isAnIncome(),
                old_name
        );
    }

    public static Body create(final Category category) {
        return create(category, null);
    }

    public String name;
    public int imageId;
    public int colorId;
    public boolean isAnIncome;
    public String old_name;

    public Body(final String name, final int imageId, final int colorId, final boolean isAnIncome, final String old_name) {
        this.name = name;
        this.imageId = imageId;
        this.colorId = colorId;
        this.isAnIncome = isAnIncome;
        this.old_name = old_name;
    }

}
