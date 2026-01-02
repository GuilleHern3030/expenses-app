package enel.dev.budgets.data.sql.external.balances;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

class Request {

    public interface Service {
        @POST("balance/")
        Call<Response> post(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @retrofit2.http.Body Body body
        );

        @PUT("balance/")
        Call<Response> put(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @retrofit2.http.Body Body body
        );

        @DELETE("balance/")
        Call<Response> delete(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @Header("Coin-Name") String coin_name,
                @Header("Coin-Symbol") String coin_symbol
        );
    }

    public static class Response {
        public boolean ok;
        public int id;
    }
}
