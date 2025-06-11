package enel.dev.budgets.objects.budget;

import java.util.ArrayList;

public class Budgets extends ArrayList<Budget> {

    private final int id;
    private final String name;

    public Budgets(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Budgets(final String name) {
        this.id = -1;
        this.name = name;
    }

    @Override
    public boolean add(final Budget budget) {
        final int index = this.indexOf(budget.getCategory().getName());
        if (index > -1) {
            Budget tmp = this.get(index);
            tmp.add(budget.getAmount());
            this.set(index, tmp);
        } else return super.add(budget);
        return true;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int indexOf(final String categoryName) {
        for (int i = 0; i < this.size(); i++)
            if (this.get(i).getCategory().getName().equals(categoryName))
                return i;
        return -1;
    }

    public double total() {
        double total = 0;
        for (Budget budget : this)
            total += budget.getAmount();
        return total;
    }

    public boolean contains(final String categoryName) {
        return indexOf(categoryName) != -1;
    }

}
