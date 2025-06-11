package enel.dev.budgets.objects.debt;

import enel.dev.budgets.objects.money.Money;

/**
 *
 */
public class Debt {

    private String lender;
    private String description;
    private Money money;
    private final int id;

    public Debt(final int id, final String lenderName, final Money debtAmount, final String description) {
        this.id = id;
        this.lender = lenderName; // prestador
        this.description = description;
        this.money = debtAmount;
    }

    public int id() { return this.id; }

    public String getLender() { return this.lender; }
    public Money getMoney() { return this.money.clone(); }
    public String getDescription() { return this.description; }


    public void setLender(final String lenderName) { this.lender = lenderName; }
    public void setDescription(final String description) { this.description = description; }
    public void setMoney(final Money money) { this.money = money; }
    public void setMoney(final int amount) { this.money.setAmount(amount); }
    public void setMoney(final double amount) { this.money.setAmount(amount); }

}
