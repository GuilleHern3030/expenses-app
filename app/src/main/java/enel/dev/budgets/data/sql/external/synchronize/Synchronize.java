package enel.dev.budgets.data.sql.external.synchronize;

import android.os.Build;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.sql.external.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;

public class Synchronize {

    public interface SynchronizeCallback {
        void onSynchronize(final String sync_code);
        void onNotFound();
        void onNetworkError();
    }

    public static class SynchronizeResponse {
        public String sync_code;
    }

    private interface ApiService {
        @GET("async/synchronize")
        Call<SynchronizeResponse> synchronizeWithCode(
                @Header("Inv-Code") String code,
                @Header("User-UUID") String uuid,
                @Header("Device-Model") String device_model
        );
    }

    public static void request(final String invitationCode, final String uuid, final SynchronizeCallback callback) {
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<SynchronizeResponse> call = apiService.synchronizeWithCode(
                invitationCode,
                uuid,
                deviceName
        );

        call.enqueue(new Callback<SynchronizeResponse>() {
            @Override
            public void onResponse(@NonNull Call<SynchronizeResponse> call, @NonNull Response<SynchronizeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final String token = response.body().sync_code;
                    callback.onSynchronize(token);
                } else {
                    callback.onNotFound();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SynchronizeResponse> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });

    }


}
