package enel.dev.budgets.objects.money;

import java.util.ArrayList;

import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;

/**
 * Un conjunto de objetos Money
 */
public class Balance {

    private ArrayList<Money> list = new ArrayList<>();

    public Balance() {

    }

    public Balance(ArrayList<Money> list) {
        ArrayList<Money> tmp = new ArrayList<>();
        for (Money money : list) tmp.add(money.clone());
        this.list = tmp;
    }

    public ArrayList<String> getCoinNames() {
        ArrayList<String> coins = new ArrayList<>();
        for (Money money : list)
            coins.add(money.getCoin().getName());
        return coins;
    }

    public Coin getCoin(final String name) {
        final int index = indexOf(name);
        return index != -1 ? list.get(index).getCoin() : null;
    }

    public int indexOf(final String name) {
        if (name != null) for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getCoin().getName().equals(name))
                return i;
        }
        return -1;
    }

    public void add(final Money money) {
        list.add(money);
    }

    public void add(final Coin coin) {
        list.add(new Money(coin, 0));
    }

    public boolean exists(final String name) {
        return indexOf(name) != -1;
    }

    public int size() {
        return this.list.size();
    }

    public Money get(final int index) {
        return list.get(index);
    }

    public Money get(final String coinName) {
        final int index = indexOf(coinName);
        return index != -1 ? get(index) : null;
    }

    public int length() {
        return this.size();
    }

    public void setAmounts(final Transactions transactions) {
        for (Transaction transaction : transactions)
            this.get(transaction.getMoney().getCoin().getName()).add(transaction.getMoney().getAmount());
    }

    public void remove(final int index) {
        list.remove(index);
    }

    public void removeDefaultCoin() {
        try {
            remove(indexOf(""));
        } catch (Exception ignored) { }
    }

    public Balance clone() {
        return new Balance(list);
    }

    public void setCoinFirst(final Coin coin) {
        try {
            final int index = indexOf(coin.getName());
            final Money tmp = list.get(index);
            final Money tmp0 = list.get(0);
            list.set(0, tmp);
            list.set(index, tmp0);
        } catch (Exception ignored) { }
    }

    public void add(final Transaction transaction) {
        try {
            final Money money = list.get(indexOf(transaction.getMoney().getCoin().getName()));
            if (transaction.isAnIncome()) money.add(transaction.getMoney().getAmount());
            else money.remove(transaction.getMoney().getAmount());
        } catch (Exception ignored) { }
    }

    public void remove(final Transaction transaction) {
        final Money money = list.get(indexOf(transaction.getMoney().getCoin().getName()));
        if (transaction.isAnIncome()) money.remove(transaction.getMoney().getAmount());
        else money.add(transaction.getMoney().getAmount());
    }

    public void edit(final Transaction oldTransaction, final Transaction newTransaction) {
        remove(oldTransaction);
        add(newTransaction);
    }

}
