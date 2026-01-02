package enel.dev.budgets.data.sql.external.transactions;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

class Request {

    public interface Service {
        @POST("transactions/")
        Call<Response> post(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @retrofit2.http.Body Body body
        );

        @PUT("transactions/")
        Call<Response> put(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @retrofit2.http.Body Body body
        );

        @DELETE("transactions/")
        Call<Response> delete(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @Header("Object-ID") int transactionId,
                @Header("Object-Date") String transactionDate
        );

        @GET("transactions/")
        Call<SelectResponse> select(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @Header("Init-Date") String dateInit,
                @Header("End-Date") String dateEnd
        );
    }

    public static class Response {
        public boolean ok;
        public int id;
    }

    public static class SelectResponse {
        public Body[] transactions;
    }
}
