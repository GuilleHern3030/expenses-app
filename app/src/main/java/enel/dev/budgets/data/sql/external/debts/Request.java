package enel.dev.budgets.data.sql.external.debts;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

class Request {

    public interface Service {
        @POST("debts/")
        Call<Response> post(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @retrofit2.http.Body Body body
        );

        @PUT("debts/")
        Call<Response> put(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @retrofit2.http.Body Body body
        );

        @DELETE("debts/")
        Call<Response> delete(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model,
                @Header("Debt-ID") int debtId
        );
    }

    public static class Response {
        public boolean ok;
        public int id;
    }
}
