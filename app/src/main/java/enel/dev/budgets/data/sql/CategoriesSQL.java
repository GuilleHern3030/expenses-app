package enel.dev.budgets.data.sql;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Comparator;

import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;

public abstract class CategoriesSQL {

    protected static final String CATEGORIES_TABLE = "CATEGORIES";
    protected final Context context;
    protected final String DATA_BASE;

    public CategoriesSQL(final Context context, final String dbName) {
        this.context = context;
        this.DATA_BASE = dbName;
    }

    static void createDefaultTables(@NonNull BasicSQL sql, final Context context) {
        final boolean tableCreated = sql.tablaCrear(CATEGORIES_TABLE, new String[]{
                "id",
                "categoryname",
                "categoryimg",
                "categorycolor",
                "categoryincome"
        });
        if (tableCreated && context != null) {
            Categories categories = Categories.defaultList(context);
            for (int i = 0; i < categories.size(); i++)
                addCategory(sql, categories.get(i));
        }
    }

    /**
     * Obtiene todas las categorías existentes
     * Las columnas son:
     *              0- id
     *              1- categoryname: nombre de la categoria
     *              2- categoryimg: id de la imagen de la categoria
     *              3- categorycolor: id del color de la categoria
     *              4- categoryincome: ¿es una categoría de 'income'? (1 para sí)
     * @return
     */
    public abstract Categories get();

    public abstract void add(final Category category, final Controller.SQLcallback callback);

    public abstract void edit(final String oldCategoryName, final Category category, final Controller.SQLcallback callback);

    public abstract void delete(final Category category, final Controller.SQLcallback callback);

    protected static int addCategory(@NonNull final BasicSQL sql, final Category category) {
        return sql.tablaIngresarFila(CATEGORIES_TABLE, new String[]{
                String.valueOf(sql.tablaFilas(CATEGORIES_TABLE)), // id
                category.getName(),
                String.valueOf(category.getImageId()),
                String.valueOf(category.getColorId()),
                category.isAnIncome() ? "1" : "0", // isAnIncome
        });
    }

}
