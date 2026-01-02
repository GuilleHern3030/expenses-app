package enel.dev.budgets.data.sql.external.categories;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.sql.BasicSQL;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.data.sql.UserController;
import enel.dev.budgets.data.sql.external.ApiClient;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.data.sql.local.CategoriesSQL;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriesSQLExternal extends enel.dev.budgets.data.sql.CategoriesSQL {

    public CategoriesSQLExternal(final Context context, final String dbName) {
        super(context, dbName);
    }

    public static void createDefaultTables(@NonNull Context context, @NonNull BasicSQL sql, Category[] categories) {
        final boolean tableCreated = sql.tablaCrear(CATEGORIES_TABLE, new String[]{
                "id",
                "categoryname",
                "categoryimg",
                "categorycolor",
                "categoryincome"
        });
        if (tableCreated) {
            if (categories.length > 0) {
                for (Category category : categories) {
                    addCategory(sql, category);
                }
            } else {
                Categories defaultcategories = Categories.defaultList(context);
                for (int i = 0; i < defaultcategories.size(); i++)
                    addCategory(sql, defaultcategories.get(i));
            }
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
        return new CategoriesSQL(context, DATA_BASE).get();
    }

    @Override
    public void add(final Category category, final Controller.SQLcallback callback) {

        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.post(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                Body.create(category)
        ).enqueue(new Callback<Request.Response>() {

            @Override
            public void onResponse(@NonNull Call<Request.Response> call, @NonNull Response<Request.Response> response) {

                if (!response.isSuccessful()) { // true si response.code() está entre 200–299
                    callback.onError("HTTP " + response.message());
                    return;
                }

                Request.Response body = response.body();
                if (body == null) {
                    callback.onError("Empty response");
                    return;
                }

                new CategoriesSQL(context, DATA_BASE).add(category, callback);

            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });
    }

    @Override
    public void edit(final String oldCategoryName, final Category category, final Controller.SQLcallback callback) {

        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.put(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                Body.create(category, oldCategoryName)
        ).enqueue(new Callback<Request.Response>() {

            @Override
            public void onResponse(@NonNull Call<Request.Response> call, @NonNull Response<Request.Response> response) {

                if (!response.isSuccessful()) { // true si response.code() está entre 200–299
                    callback.onError("HTTP " + response.message());
                    return;
                }

                Request.Response body = response.body();
                if (body == null) {
                    callback.onError("Empty response");
                    return;
                }

                new CategoriesSQL(context, DATA_BASE).edit(oldCategoryName, category, callback);

            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });

    }

    @Override
    public void delete(final Category category, final Controller.SQLcallback callback) {

        final Request.Service api = ApiClient.getClient().create(Request.Service.class);
        api.delete(
                UserController.getID(context),
                UserController.getUUID(context),
                UserController.getSyncCode(context),
                "Bearer " + UserController.getToken(context),
                Build.MANUFACTURER + " " + Build.MODEL,
                category.getName()
        ).enqueue(new Callback<Request.Response>() {

            @Override
            public void onResponse(@NonNull Call<Request.Response> call, @NonNull Response<Request.Response> response) {

                if (!response.isSuccessful()) { // true si response.code() está entre 200–299
                    callback.onError(response.message());
                    return;
                }

                Request.Response body = response.body();
                if (body == null) {
                    callback.onError("Empty response");
                    return;
                }

                new CategoriesSQL(context, DATA_BASE).delete(category, callback);

            }

            @Override
            public void onFailure(@NonNull Call<Request.Response> call, @NonNull Throwable t) {
                callback.onNetworkError(); // Problema de red
            }
        });

    }

}
