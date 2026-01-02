package enel.dev.budgets.data.sql.external.synchronize;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.sql.UserController;
import enel.dev.budgets.data.sql.external.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;

public class Desynchronize {

    public interface DesynchronizeCallback {
        void onDesynchronize(final boolean ok);
        void onNetworkError();
    }

    public static class DesynchronizeResponse {
        public boolean ok;
    }

    private interface ApiService {
        @GET("async/desynchronize")
        Call<DesynchronizeResponse> desynchronize(
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Device-Model") String device_model
        );
    }

    public static void request(final Context context, final DesynchronizeCallback callback) {

        final String uuid = UserController.getUUID(context);
        final String sync_code = UserController.getSyncCode(context);
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<DesynchronizeResponse> call = apiService.desynchronize(
                uuid,
                sync_code,
                deviceName
        );

        call.enqueue(new Callback<DesynchronizeResponse>() {
            @Override
            public void onResponse(@NonNull Call<DesynchronizeResponse> call, @NonNull Response<DesynchronizeResponse> response) {
                if (response.isSuccessful() && response.body() != null)
                    callback.onDesynchronize(response.body().ok);
            }

            @Override
            public void onFailure(@NonNull Call<DesynchronizeResponse> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });

    }


}
