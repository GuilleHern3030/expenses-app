package enel.dev.budgets.data.sql.external.balances;

import androidx.annotation.Nullable;

import enel.dev.budgets.objects.money.Coin;

class Body {
    public static Body create(final Coin coin, @Nullable final String old_name) {
        return new Body(
                coin.getName(),
                coin.getSymbol(),
                old_name
        );
    }

    public static Body create(final Coin coin) {
        return create(coin, null);
    }

    public String name;
    public String symbol;
    public String old_name;

    public Body(final String name, final String symbol, final String old_name) {
        this.name = name;
        this.symbol = symbol;
        this.old_name = old_name;
    }

}
