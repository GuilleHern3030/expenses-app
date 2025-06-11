package enel.dev.budgets.objects.shoppinglist;

public class Item {

    private boolean completed;
    private final String name;

    public Item(final String name, final boolean completed) {
        this.name = name;
        this.completed = completed;
    }

    public Item(final String name) {
        this(name, false);
    }

    public String getName() {
        return this.name;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public void setCompleted(final boolean completed) {
        this.completed = completed;
    }

    public void setCompleted() {
        this.completed = true;
    }

    public void setIncompleted() {
        this.completed = false;
    }

    public String join() {
        return this.completed ?
                this.name + "1":
                this.name + "0";
    }

}
