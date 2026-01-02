package enel.dev.budgets.data.sql.external.synchronize;


import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.sql.UserController;
import enel.dev.budgets.data.sql.external.ApiClient;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.debt.Debt;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.reserve.Reserve;
import enel.dev.budgets.objects.shoppinglist.Item;
import enel.dev.budgets.objects.transaction.Transaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;

public class Pull {

    public interface PullCallback {
        void onSuccess(Coin[] balance, Category[] categories, Debt[] debts, Item[] shoppingListItems, Transaction[] transactions, Reserve[] reserves, Date date);
        void onError(int errorCode);
        void onNetworkError();
    }

    private interface ApiService {
        @GET("async/")
        Call<PullResponse> pull(
                @Header("User-ID") int id,
                @Header("User-UUID") String uuid,
                @Header("Sync-Code") String sync_code,
                @Header("Authorization") String authorization,
                @Header("Device-Model") String device_model
        );
    }

    private static class PullResponse {
        public CoinResponse[] coins;
        public CategoryResponse[] categories;
        public DebtResponse[] debts;
        public ShoppingListResponse[] shoppingListItems;
        public TransactionResponse[] transactions;
        public ReserveResponse[] reserves;
    }

    private static class CategoryResponse {
        public String name;
        public int imageId;
        public int colorId;
        public boolean isAnIncome;
    }

    private static class DebtResponse {
        public String lender;
        public String description;
        public int id;

        // Money
        public String coin_name; // Cantidad transaccionada (Objeto Coin)
        public String coin_symbol; // Cantidad transaccionada (Objeto Coin)
        public double amount; // Cantidad transaccionada
    }

    private static class ShoppingListResponse {
        public boolean completed;
        public String name;

    }

    private static class CoinResponse {
        public String name;
        public String symbol;
    }

    private static class ReserveResponse {
        public int id;
        public String name;
        public double amount;
    }

    private static class TransactionResponse {
        public long date; // Fecha de la transaccion (YYYYMMDDHH)
        public String description; // Descripcion de la transaccion
        public boolean isAnIncome; // ¿Es un ingreso?
        public int id;

        // Category
        public String category_name;
        public int category_imageId;
        public int category_colorId;

        // Money
        public String coin_name; // Cantidad transaccionada (Objeto Coin)
        public String coin_symbol; // Cantidad transaccionada (Objeto Coin)
        public double amount; // Cantidad transaccionada
    }

    public static void load(final Context context, final PullCallback callback) {
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;

        ApiService api = ApiClient.getClient().create(ApiService.class);

        final int id = UserController.getID(context);
        final String uuid = UserController.getUUID(context);
        final String sync_code = UserController.getSyncCode(context);
        final String token = UserController.getToken(context);

        api.pull(
                id,
                uuid,
                sync_code,
                "Bearer " + token,
                deviceName
            ).enqueue(new Callback<PullResponse>() {
            @Override
            public void onResponse(@NonNull Call<PullResponse> call, @NonNull Response<PullResponse> response) {
                if (response.isSuccessful()) {

                    if (response.body() != null) {

                        callback.onSuccess(
                                wrap(response.body().coins),
                                wrap(response.body().categories),
                                wrap(response.body().debts),
                                wrap(response.body().shoppingListItems),
                                wrap(response.body().transactions),
                                wrap(response.body().reserves),
                                new Date()
                        );

                    } else callback.onError(404);

                /*}
                else if(response.body() != null) {
                    callback.onError(new Exception(response.body().message));*/
                } else {
                    callback.onError(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PullResponse> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });


    }

    // Wrappers
    private static Coin[] wrap(CoinResponse[] responses) {
        Coin[] wrapped = new Coin[responses.length];
        for (int i = 0; i < responses.length; i++) {
            CoinResponse r = responses[i];
            wrapped[i] = new Coin(r.name, r.symbol);
        }
        return wrapped;
    }

    private static Category[] wrap(CategoryResponse[] responses) {
        Category[] wrapped = new Category[responses.length];
        for (int i = 0; i < responses.length; i++) {
            CategoryResponse r = responses[i];
            wrapped[i] = new Category(r.name, r.imageId, r.colorId, r.isAnIncome);
        }
        return wrapped;
    }

    private static Debt[] wrap(DebtResponse[] responses) {
        Debt[] wrapped = new Debt[responses.length];
        for (int i = 0; i < responses.length; i++) {
            DebtResponse r = responses[i];
            wrapped[i] = new Debt(r.id, r.lender, new Money(r.coin_name, r.coin_symbol, r.amount), r.description);
        }
        return wrapped;
    }
    private static Reserve[] wrap(ReserveResponse[] responses) {
        Reserve[] wrapped = new Reserve[responses.length];
        for (int i = 0; i < responses.length; i++) {
            ReserveResponse r = responses[i];
            wrapped[i] = new Reserve(r.id, r.name, r.amount);
        }
        return wrapped;
    }

    private static Item[] wrap(ShoppingListResponse[] responses) {
        Item[] wrapped = new Item[responses.length];
        for (int i = 0; i < responses.length; i++) {
            ShoppingListResponse r = responses[i];
            wrapped[i] = new Item(r.name, r.completed);
        }
        return wrapped;
    }

    private static Transaction[] wrap(TransactionResponse[] responses) {
        Transaction[] wrapped = new Transaction[responses.length];
        for (int i = 0; i < responses.length; i++) {
            try {
                TransactionResponse r = responses[i];
                wrapped[i] = new Transaction(
                        r.id,
                        new Category(r.category_name, r.category_imageId, r.category_colorId, r.isAnIncome),
                        new Date(r.date),
                        new Money(r.coin_name, r.coin_symbol, r.amount),
                        r.description,
                        r.isAnIncome
                );
            } catch(Exception e) { wrapped[i] = null; }
        }
        return wrapped;
    }

}
