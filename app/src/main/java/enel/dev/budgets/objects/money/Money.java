package enel.dev.budgets.objects.money;

import androidx.annotation.NonNull;

import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.objects.NumberFormat;

/**
 *  Un objeto que representa una cantidad de dinero, en una moneda específica con su respectivo símbolo
 */

public class Money {

    private double amount;
    private final Coin coin;

    //<editor-fold defaultstate="collapsed" desc=" Constructor ">
    public Money (final Coin coin, final double amount) {
        this.coin = coin;
        this.amount = amount;
    }

    public Money (final String coinName, final String coinSymbol, final double amount) {
        this.coin = new Coin(coinName, coinSymbol);
        this.amount = amount;
    }

    public Money (final String coinName, final String coinSymbol, final int amount) {
        this(coinName, coinSymbol, (double)amount);
    }

    public Money (final String coinName, final String coinSymbol) {
        this(coinName, coinSymbol, 0);
    }

    public Money (final String coinName) {
        this(coinName, Preferences.DEFAULT_COIN_SYMBOL, 0);
    }

    public Money (final String coinName, final int amount) {
        this(coinName, Preferences.DEFAULT_COIN_SYMBOL, (double)amount);
    }

    public Money (final String coinName, final double amount) {
        this(coinName, Preferences.DEFAULT_COIN_SYMBOL, amount);
    }

    public Money (final double amount) {
        this("", Preferences.DEFAULT_COIN_SYMBOL, amount);
    }

    public Money (final int amount) {
        this("", Preferences.DEFAULT_COIN_SYMBOL, (double)amount);
    }
    //</editor-fold>

    public void setAmount(final double amount) { this.amount = amount; }

    public void setAmount(final int amount) { this.amount = (double)amount; }

    public void add(final double amount) { this.amount += amount; }

    public void remove(final double amount) { this.amount -= amount; }

    public String name() { return this.coin.getName(); }

    public String symbol() { return this.coin.getSymbol(); }

    public double getAmount() { return this.amount; }

    public Coin getCoin() { return this.coin; }

    public String value(final NumberFormat decimalFormat) {
        return decimalFormat.toString(this.amount);
    }

    public String value() {
        return Preferences.DEFAULT_DECIMAL_FORMAT.toString((this.amount));
    }

    @NonNull
    @Override
    public String toString() {
        return this.toString(Preferences.DEFAULT_DECIMAL_FORMAT);
    }

    public String toString(final NumberFormat decimalFormat) {
        String symbol = symbol().length() != 0 ? this.coin.getSymbol() + " " : "";
        return symbol + decimalFormat.toString(this.amount);
    }

    public Money clone() {
        final double moneyAmount = this.amount;
        return new Money(this.coin, moneyAmount);
    }

    public static String value(final double amount, final NumberFormat decimalFormat) {
        return new Money(amount).value(decimalFormat);
    }

    public static String toString(final String coinSymbol, final double amount, final NumberFormat decimalFormat) {
        return new Money(new Coin(coinSymbol), amount).toString(decimalFormat);
    }

    public String serialize() {
        String coinName = this.coin != null ? this.coin.getName() : "";
        String coinSymbol = this.coin != null ? this.coin.getSymbol() : "";
        String amount = String.valueOf(this.amount);

        return "{coin:" + coinName + "," +
                "symbol:" + coinSymbol + "," +
                "amount:" + amount + "}";
    }

    public static Money newInstance(final String serializedMoney) {
        if (serializedMoney != null && serializedMoney.charAt(0) == '{' && serializedMoney.charAt(serializedMoney.length()-1) == '}') {
            final String json = serializedMoney.substring(1, serializedMoney.length() - 1);
            final String[] objects = json.split(",");
            if (objects.length == 3) try {
                final String coinName = objects[0].substring(objects[0].indexOf(':')+1);
                final String coinSymbol = objects[1].substring(objects[1].indexOf(':')+1);
                final double amount = Double.parseDouble(objects[2].substring(objects[2].indexOf(':')+1));
                return new Money(coinName, coinSymbol, amount);
            } catch (Exception ignored) { }
        } return null;
    }

}
