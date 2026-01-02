package enel.dev.budgets.data.sql.external;


import android.os.Build;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static final String baseURL = !Build.MODEL.startsWith("sdk_") ? "https://expensesapp.guillenh.com/" : "http://10.0.2.2:5430/";
    //public static final String baseURL = "https://expensesapp.guillenh.com/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }


}

