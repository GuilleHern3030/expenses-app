package enel.dev.budgets.objects.budget;

import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.money.Money;

public class Budget {

    private final Category category;
    private double money;

    public Budget(final Category category, final double moneyAmount) {
        this.category = category;
        this.money = moneyAmount;
    }

    public Budget(final Category category) {
        this.category = category;
        this.money = 0;
    }

    public Budget(final int id, final Category category, final Money money) {
        this.category = category;
        this.money = money.getAmount();
    }

    public Category getCategory() {
        return this.category;
    }

    public double getAmount() {
        return this.money;
    }

    public void add(final double amount) {
        this.money += amount;
    }

}
