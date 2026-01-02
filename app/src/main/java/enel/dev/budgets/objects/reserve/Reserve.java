package enel.dev.budgets.objects.reserve;

import enel.dev.budgets.objects.money.Money;

public class Reserve {


    private final int id;
    private final String name;
    private final double amount;

    public Reserve (final int id, final String name, final double amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }

    public int id() { return this.id; }
    public String getName() { return this.name; }
    public double getAmount() { return this.amount; }

}
