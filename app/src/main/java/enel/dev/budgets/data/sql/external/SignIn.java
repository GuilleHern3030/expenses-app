package enel.dev.budgets.data.sql.external;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.Executors;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.UserController;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Header;
import retrofit2.http.PUT;

public class SignIn {

    public interface TokenCallback {
        void onTokenGetted(String token);
        void onError(Exception e);
    }

    public interface AuthCallback {
        void onAuthenticate(final AuthResponse authResponse);
        void onNetworkError();
        void onError();
    }

    public static void getGoogleToken(final Activity context, final TokenCallback tokenCallback) {

        GetGoogleIdOption googleIdOption =
                new GetGoogleIdOption.Builder()
                        .setServerClientId(context.getString(R.string.server_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build();

        GetCredentialRequest request =
                new GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build();


        CredentialManager credentialManager = CredentialManager.create(context);

        credentialManager.getCredentialAsync(
                context,
                request,
                null,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {

                    @Override
                    public void onResult(GetCredentialResponse result) {

                        Credential credential = result.getCredential();

                        if (credential instanceof CustomCredential) {

                            CustomCredential customCredential = (CustomCredential) credential;

                            if (customCredential.getType()
                                    .equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {

                                GoogleIdTokenCredential googleCredential =
                                        GoogleIdTokenCredential.createFrom(customCredential.getData());

                                final String idToken = googleCredential.getIdToken(); // JWT

                                tokenCallback.onTokenGetted(idToken);

                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        tokenCallback.onError(e);
                    }
                }
        );
    }


    public static class AuthResponse {
        public int id;
        public String token;
        public String email;
        public boolean is_premium;
        public String sync_code; // hashed | null si no es premium
    }

    private interface ApiService {
        @PUT("user/googletoken")
        Call<AuthResponse> loginWithGoogle(
                @Header("Authorization") String authorization,
                @Header("User-UUID") String uuid,
                @Header("Device-Model") String deviceModel
        );
    }

    /**
     * Procesa el token obtenido de Google
     * @param googleToken token obtenido del servicio de Google CredentialManager en getGoogleToken
     * @param authCallback respuesta de la función
     */
    public static void processToken(final Activity context, final String googleToken, AuthCallback authCallback) {
        final String uuid = UserController.getUUID(context);
        final String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<AuthResponse> call = apiService.loginWithGoogle(
                "Bearer " + googleToken,
                uuid,
                deviceModel
        );

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    authCallback.onAuthenticate(auth);
                } else { authCallback.onError(); }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                authCallback.onNetworkError();
            }
        });

    }

}
