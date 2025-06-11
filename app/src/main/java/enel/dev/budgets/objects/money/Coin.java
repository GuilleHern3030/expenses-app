package enel.dev.budgets.objects.money;

import androidx.annotation.NonNull;

public class Coin {

    private final String name;
    private final String symbol;

    public Coin(final String name, final String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public Coin(final String symbol) {
        this.name = "";
        this.symbol = symbol;
    }

    public String getName () {
        return this.name;
    }
    public String getSymbol () { return this.symbol; }

    @NonNull
    @Override
    public String toString() {
        return this.symbol;
    }

}
