package enel.dev.budgets.data.sql;

import android.app.Activity;
import android.content.Context;

import static enel.dev.budgets.data.sql.Controller.DATA_BASE_LOCAL;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;

import enel.dev.budgets.R;
import enel.dev.budgets.data.sql.external.SignIn;
import enel.dev.budgets.objects.Date;

/**
 * Keys: use_cloud, is_premium, last_pull, is_signed_in, token, user_id, user_email, sync_code, uuid
 */
public class UserController {

    static final String USER_TABLE = "USER_DATA";

    private final static int KEY = 0;
    private final static int VALUE = 1;

    static void createDefaultTables(@NonNull BasicSQL sql) {
        sql.tablaCrear(USER_TABLE, new String[]{
                "key", // 0
                "value" // 1
        });
    }

    private static String getValue(final Context context, final String key) {
        String value = null;
        final BasicSQL sql = new BasicSQL(context, DATA_BASE_LOCAL);
        try {
            final int row = sql.tablaBuscarFila(USER_TABLE, "key", key, true);
            if (row >= 0)
                value = sql.tablaObtenerFila(USER_TABLE, row)[VALUE];
        } catch (Exception ignored) { }
        sql.cerrar();
        return value;
    }

    private static void setValue(final Context context, final String key, final boolean value) {
        setValue(context, key, value ? "1" : "0");
    }

    private static void setValue(final Context context, final String key, final String value) {
        final String[] data = new String[]{ key, value };
        final BasicSQL sql = new BasicSQL(context, DATA_BASE_LOCAL);
        final int row = sql.tablaBuscarFila(USER_TABLE, "key", key, true);
        if (row >= 0) sql.tablaEditarFila(USER_TABLE, row, data);
        else sql.tablaIngresarFila(USER_TABLE, data);
        sql.cerrar();
    }

    /**
     * Verificar si la Base de Datos existe
     * @param context Activity
     * @return True si la Base de Datos existe
     */
    public static boolean exists(final Context context) {
        final ArrayList<String> dbs = new ArrayList<>(Arrays.asList(BasicSQL.listarBasesDeDatos(context)));
        return dbs.contains(DATA_BASE_LOCAL);
    }

    public static boolean isPremium(final Context context) {
        return Objects.equals(getValue(context, "is_premium"), "1");
    }

    public static String getUUID(final Context context) {
        String uuid = getValue(context, "uuid");
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            setValue(context, "uuid", uuid);
        }
        return uuid;
    }

    public static int getID(final Context context) {
        String rawId = getValue(context, "id");
        if (rawId != null) try {
            return Integer.parseInt(rawId);
        } catch(Exception ignored) { }
        return -1;
    }

    public static String getSyncCode(final Context context) {
        return getValue(context, "sync_code");
    }

    public static String getToken(final Context context) {
        return getValue(context, "token");
    }

    public static boolean useCloud(final Context context) {
        return Objects.equals(getValue(context, "use_cloud"), "1");
    }

    public static void setUseCloud(final Context context, final boolean use) {
        setValue(context, "use_cloud", use);
    }

    public static void updateLastPull(final BasicSQL sql) {
        long timestamp = System.currentTimeMillis();
        final int row = sql.tablaBuscarFila(USER_TABLE, "key", "last_pull", true);
        final String[] data = new String[]{ "last_pull", String.valueOf(timestamp) };
        if (row > 0) sql.tablaEditarFila(USER_TABLE, row, data);
        else sql.tablaIngresarFila(USER_TABLE, data);
    }

    public static long lastPull (final Context context) {
        try {
            return Long.parseLong(getValue(context, "last_pull"));
        } catch(Exception e) {
            return 0;
        }
    }

    public static boolean isSignedIn(final Context context) {
        final String signedIn = getValue(context, "is_signed_in");
        return signedIn != null && !Objects.equals(signedIn, "0");
    }

    /**
     * Verifica si el usuario está sincronizado con el backend
     * @param context context
     * @return {boolean} TRUE si tiene sync_code o premium
     */
    public static boolean isSynchronized(final Context context) {
        final String sync_code = getValue(context, "sync_code");
        return sync_code != null && sync_code.length() > 0 || isPremium(context);
    }

    public static void setSyncCode(final Context context, final String sync_code) {
        setValue(context, "sync_code", sync_code);
    }

    public static void unsync(final Context context) {
        setValue(context, "sync_code", null);
        setValue(context, "use_cloud", false);
    }

    public interface SignCallback {
        void onSuccess(final SignIn.AuthResponse authResponse);
        void onError(Exception e);
    }

    public static void signIn(final Activity context, final SignCallback signCallback) {

        SignIn.getGoogleToken(context, new SignIn.TokenCallback() {
            @Override
            public void onTokenGetted(final String token) {
                SignIn.processToken(context, token, new SignIn.AuthCallback() {
                    @Override
                    public void onAuthenticate(SignIn.AuthResponse authResponse) { // Logeado

                        final BasicSQL sql = new BasicSQL(context, DATA_BASE_LOCAL);
                        sql.tablaEliminar(USER_TABLE);
                        createDefaultTables(sql);
                        sql.tablaIngresarFila(USER_TABLE, new String[]{ "use_cloud", "0" });
                        sql.tablaIngresarFila(USER_TABLE, new String[]{ "is_premium", authResponse.is_premium ? "1" : "0" });
                        sql.tablaIngresarFila(USER_TABLE, new String[]{ "last_pull", "0" });
                        sql.tablaIngresarFila(USER_TABLE, new String[]{ "is_signed_in", "1" });
                        sql.tablaIngresarFila(USER_TABLE, new String[]{ "token", String.valueOf(authResponse.token) });
                        sql.tablaIngresarFila(USER_TABLE, new String[]{ "user_id", String.valueOf(authResponse.id) });
                        sql.tablaIngresarFila(USER_TABLE, new String[]{ "user_email", String.valueOf(authResponse.email) });
                        sql.tablaIngresarFila(USER_TABLE, new String[]{ "sync_code", authResponse.sync_code != null ? authResponse.sync_code : "" });
                        sql.cerrar();

                        signCallback.onSuccess(authResponse);
                    }

                    @Override
                    public void onNetworkError() {
                        signCallback.onError(null);
                    }

                    @Override
                    public void onError() {
                        signCallback.onError(null);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                signCallback.onError(e);
            }
        });

    }

    public static void signOut(final Activity context) {

        final BasicSQL sql = new BasicSQL(context, DATA_BASE_LOCAL);
        sql.tablaEliminar(USER_TABLE);
        sql.cerrar();

    }

}
