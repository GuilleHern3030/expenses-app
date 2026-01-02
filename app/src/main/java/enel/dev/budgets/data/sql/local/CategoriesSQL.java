package enel.dev.budgets.data.sql.local;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Comparator;

import enel.dev.budgets.data.sql.BasicSQL;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;

public class CategoriesSQL extends enel.dev.budgets.data.sql.CategoriesSQL {

    public CategoriesSQL(final Context context, final String dbName) {
        super(context, dbName);
    }

    public static void createDefaultTables(@NonNull Context context, @NonNull BasicSQL sql) {
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
    @Override
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

    @Override
    public void add(final Category category, final Controller.SQLcallback callback) {
        if (category == null) { callback.onError("Category invalid"); return; }
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        Categories categories = getCategories(sql);
        if (categories.exists(category.getName())) { callback.onError("Category invalid"); return; }
        boolean success = addCategory(sql, category) >= 0;
        sql.cerrar();
        if (success)
            callback.onSuccess();
        else callback.onError("");
    }

    @Override
    public void edit(final String oldCategoryName, final Category category, final Controller.SQLcallback callback) {
        BasicSQL sql = new BasicSQL(context, DATA_BASE);
        boolean success = false;
        if (category != null) try {
            final int row = sql.tablaBuscarFila(CATEGORIES_TABLE, "categoryname", oldCategoryName, false);
            if (row >= 0) {
                success = sql.tablaEditarFila(CATEGORIES_TABLE, row, new String[]{
                        "", // ID
                        category.getName(), // categoryname
                        String.valueOf(category.getImageId()), // categoryimg
                        String.valueOf(category.getColorId()), // categorycolor
                        category.isAnIncome() ? "1" : "0" // categoryincome
                });
            }
        } catch (Exception ignored) { }
        sql.cerrar();
        if (success) callback.onSuccess();
        else callback.onError("");
    }

    @Override
    public void delete(final Category category, final Controller.SQLcallback callback) {
        boolean success = false;
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        if (category != null) try {
            final int row = sql.tablaBuscarFila(CATEGORIES_TABLE, "categoryname", category.getName(), false);
            if (row >= 0)
                success = sql.tablaEliminarFila(CATEGORIES_TABLE, row, true);
        } catch(Exception ignored) { }
        sql.cerrar();
        if (success) callback.onSuccess();
        else callback.onError("");
    }

}
