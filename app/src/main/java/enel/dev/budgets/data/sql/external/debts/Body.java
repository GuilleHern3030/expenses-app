package enel.dev.budgets.data.sql.external.debts;

import enel.dev.budgets.objects.debt.Debt;

class Body {
    public static Body create(final Debt debt, final int id) {
        return new Body(
                id,
                debt.getLender(),
                debt.getDescription(),
                debt.getMoney().getCoin().getName(),
                debt.getMoney().getCoin().getSymbol(),
                debt.getMoney().getAmount()
        );
    }

    public static Body create(final Debt debt) {
        return create(debt, debt.id());
    }

    public static Debt recreate(final Debt debt, final int id) {
        if (debt != null) {
            return new Debt(
                    id,
                    debt.getLender(),
                    debt.getMoney(),
                    debt.getDescription()
            );
        } else return null;
    }

    public int id;
    public String lender;
    public String description;

    // Money
    public String coin_name; // Cantidad transaccionada (Objeto Coin)
    public String coin_symbol; // Cantidad transaccionada (Objeto Coin)
    public double amount; // Cantidad transaccionada

    public Body(
            final int id, final String lender, final String description,
            final String coin_name, final String coin_symbol, final double amount) {

        this.id = id;
        this.lender = lender;
        this.description = description;
        this.coin_name = coin_name;
        this.coin_symbol = coin_symbol;
        this.amount = amount;

    }
}
