package enel.dev.budgets.data.sql.external.shoppinglist;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

class Request {

    public interface Service {
        @POST("shoppinglist/")
        Call<Response> post(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @Header("Item-Name-New") String newItemName
        );

        @PUT("shoppinglist/")
        Call<Response> put(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @Header("Item-Name-New") String newItemName,
                @Header("Item-Name-Old") String oldItemName
        );

        @DELETE("shoppinglist/")
        Call<Response> delete(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @Header("Item-Name") String itemName
        );
    }

    public static class Response {
        public boolean ok;
    }
}
