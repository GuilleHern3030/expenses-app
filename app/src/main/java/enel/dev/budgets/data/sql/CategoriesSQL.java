package enel.dev.budgets.data.sql;

import static enel.dev.budgets.data.sql.Controller.DATA_BASE;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Comparator;

import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;

public class CategoriesSQL {

    static final String CATEGORIES_TABLE = "CATEGORIES";
    private final Context context;

    public CategoriesSQL(final Context context) {
        this.context = context;
    }

    static void createDefaultTables(@NonNull Context context, @NonNull BasicSQL sql) {
        final boolean tableCreated = sql.tablaCrear(CATEGORIES_TABLE, new String[]{
                "id",
                "categoryname",
                "categoryimg",
                "categorycolor",
                "categoryincome"
        });
        if (tableCreated) {
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
    public Categories get() {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        Categories categories = getCategories(sql);
        sql.cerrar();
        return categories;
    }

    static Categories getCategories(@NonNull BasicSQL sql) {
        final Categories categories = new Categories();
        try {
            final int categoriesSize = sql.tablaFilas(CATEGORIES_TABLE);
            for (int i = 0; i < categoriesSize; i++) {
                final String[] col = sql.tablaObtenerFila(CATEGORIES_TABLE, i);
                categories.add(new Category(
                        col[1], // nombre de la categoría
                        Integer.parseInt(col[2]), // id de la imagen de la categoría
                        Integer.parseInt(col[3]), // id del color de la categoría
                        Integer.parseInt((col[4])) == 1 // isAnIncome?
                ));
            }
            categories.sort(Comparator.comparing(Category::getName));
        } catch(Exception ignored) { }
        return categories;
    }

    private static int addCategory(@NonNull final BasicSQL sql, final Category category) {
        return sql.tablaIngresarFila(CATEGORIES_TABLE, new String[]{
                String.valueOf(sql.tablaFilas(CATEGORIES_TABLE)), // id
                category.getName(),
                String.valueOf(category.getImageId()),
                String.valueOf(category.getColorId()),
                category.isAnIncome() ? "1" : "0", // isAnIncome
        });
    }

    public boolean add(final Category category) {
        if (category == null) return false;
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        Categories categories = getCategories(sql);
        if (categories.exists(category.getName())) return false;
        boolean success = addCategory(sql, category) >= 0;
        sql.cerrar();
        return success;
    }

    public boolean edit(final String oldCategoryName, final Category category) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        if (category != null) try {
            final int row = sql.tablaBuscarFila(CATEGORIES_TABLE, "categoryname", oldCategoryName, false);
            if (row >= 0) {
                return sql.tablaEditarFila(CATEGORIES_TABLE, row, new String[]{
                        "", // ID
                        category.getName(), // categoryname
                        String.valueOf(category.getImageId()), // categoryimg
                        String.valueOf(category.getColorId()), // categorycolor
                        category.isAnIncome() ? "1" : "0" // categoryincome
                });
            }
        } catch (Exception ignored) { }
        sql.cerrar();
        return false;
    }

    public boolean delete(final Category category) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        if (category != null) try {
            final int row = sql.tablaBuscarFila(CATEGORIES_TABLE, "categoryname", category.getName(), false);
            if (row >= 0)
                return sql.tablaEliminarFila(CATEGORIES_TABLE, row, true);
        } catch(Exception ignored) { }
        sql.cerrar();
        return false;
    }

}
