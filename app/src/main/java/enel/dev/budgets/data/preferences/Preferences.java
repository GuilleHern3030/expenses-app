package enel.dev.budgets.data.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Currency;
import java.util.Locale;

import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.objects.money.Coin;

public class Preferences {

    private static final String PREFERENCES_FILE = "default";
    private static final String DEFAULT_COIN_NAME_KEY = "defaultcoinname";
    private static final String DEFAULT_COIN_SYMBOL_KEY = "defaultcoinsymbol";
    private static final String DEFAULT_DECIMAL_FORMAT_KEY = "decimalformat";
    private static final String PASSWORD_KEY = "accesspass";
    private static final String MORE_COINS_ACTIVED = "morecoins";

    public static final int DEFAULT_DECIMALS_AMOUNT = 2;

    public static final NumberFormat DEFAULT_DECIMAL_FORMAT = NumberFormat.COMMA_DOT;
    public static final String DEFAULT_COIN_SYMBOL = "$";
    public static final String DEFAULT_COIN_NAME = "";

    public static Coin defaultCoin(final Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        String coinName = sp.getString(DEFAULT_COIN_NAME_KEY, "");
        String coinSymbol = sp.getString(DEFAULT_COIN_SYMBOL_KEY, "");
        return new Coin(coinName, coinSymbol);
    }

    public static NumberFormat decimalFormat(final Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        String data = sp.getString(DEFAULT_DECIMAL_FORMAT_KEY, String.valueOf(DEFAULT_DECIMAL_FORMAT.get()));
        return NumberFormat.newInstance(data.charAt(0));
    }

    public static boolean moreCoinsAvailable(final Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sp.getBoolean(MORE_COINS_ACTIVED, false);
    }

    public static String password(final Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sp.getString(PASSWORD_KEY, "");
    }

    public static void setDefaultCoin(final Activity activity, final Coin coin) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEFAULT_COIN_NAME_KEY, coin.getName());
        editor.putString(DEFAULT_COIN_SYMBOL_KEY, coin.getSymbol());
        editor.apply();
    }

    public static void setDefaultCoin(final Activity activity, final String symbol) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEFAULT_COIN_NAME_KEY, DEFAULT_COIN_NAME);
        editor.putString(DEFAULT_COIN_SYMBOL_KEY, symbol);
        editor.apply();
    }

    public static void setMoreCoinsAvailable(final Activity activity, final boolean available) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(MORE_COINS_ACTIVED, available);
        editor.apply();
    }

    /**
     * Establece el formato de los decimales por defecto
     * @param activity context
     * @param decimalFormat number format
     */
    public static void setDecimalFormat(final Activity activity, final NumberFormat decimalFormat) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEFAULT_DECIMAL_FORMAT_KEY, Character.toString(decimalFormat.toChar()));
        editor.apply();
    }

    public static void setPassword(final Activity activity, final String password) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PASSWORD_KEY, password);
        editor.apply();
    }

    public static void loadDefaultData(final Activity activity) {
        if (defaultCoin(activity).getSymbol().isEmpty()) try {
            Locale locale = Locale.getDefault();
            Currency currency = Currency.getInstance(locale);
            String coinSymbol = currency.getSymbol();
            setDefaultCoin(activity, coinSymbol);
        } catch(Exception ignored) {
            setDefaultCoin(activity, new Coin(DEFAULT_COIN_NAME, DEFAULT_COIN_SYMBOL));
        }
    }

}
