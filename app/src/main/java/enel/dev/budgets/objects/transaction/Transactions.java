package enel.dev.budgets.objects.transaction;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.money.Money;

/**
 *  Un conjunto de transacciones
 *  Pueden ser transacciones de tipo 'income' o de tipo 'expense' pero no ambas mezcladas
 *  Permite además obtener un resumen de las transacciones
 */
public class Transactions extends ArrayList<Transaction> {

    /**
     * Junta todas las transacciones en sus respectivas categorías
     * @return Un conjunto de transacciones sin categorías repetidas
     */
    public Transactions getCategoriesSorted() {
        if (this.size() > 0) {
            ArrayList<Transaction> unorderedCategories = unifyCategories(this.clone());
            ArrayList<Transaction> orderedCategories = sortCategories(unorderedCategories);
            Transactions orderedTransactions = new Transactions();
            orderedTransactions.addAll(orderedCategories);
            return orderedTransactions;
        } else return new Transactions();
    }

    /**
     * Obtiene el index de la transacción con el id especificado
     * @param id id de la transaccion buscada
     * @return Devuelve la Transaccion con el id especificado o -1 si no lo encuentra
     */
    public int getId(final int id) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).id() == id)
                return i;
        }
        return -1;
    }

    /**
     * Obtiene todas las transacciones del tipo de categoría definido
     * @return Devuelve un conjunto de transacciones de la categoría seleccionada
     */
    public Transactions filterCategory(final String categoryName) {
        Transactions transactions = new Transactions();
        for (int i = 0; i < this.size(); i++)
            if (this.get(i).getCategory().getName().equals(categoryName))
                transactions.add(this.get(i).clone());
        return transactions;
    }

    /**
     * Obtiene todas las transacciones de la moneda definida
     * @return Devuelve un conjunto de transacciones de la moneda seleccionada
     */
    public Transactions filterCoin(final String coinName) {
        Transactions transactions = new Transactions();
        for (int i = 0; i < this.size(); i++)
            if (this.get(i).getMoney().getCoin().getName().equals(coinName))
                transactions.add(this.get(i).clone());
        return transactions;
    }

    /**
     * Obtiene todas las transacciones de tipo 'income'
     * @return Devuelve un conjunto de transacciones de tipo 'income'
     */
    public Transactions incomes() {
        Transactions incomes = new Transactions();
        for (int i = 0; i < this.size(); i++)
            if (this.get(i).isAnIncome())
                incomes.add(this.get(i).clone());
        return incomes;
    }

    /**
     * Obtiene todas las transacciones de tipo 'expense'
     * @return Devuelve un conjunto de transacciones de tipo 'expense'
     */
    public Transactions expenses() {
        Transactions expenses = new Transactions();
        for (int i = 0; i < this.size(); i++)
            if (!this.get(i).isAnIncome())
                expenses.add(this.get(i).clone());
        return expenses;
    }

    /**
     * Obtiene todas las transacciones, convirtiendo a las de tipo 'expense' en un número negativo
     * @return Devuelve un conjunto de transacciones de tipo 'expense' e 'income' en conjunto
     */
    public Transactions balance() {
        Transactions balance = new Transactions();
        for (int i = 0; i < this.size(); i++) try {
            if (!this.get(i).isAnIncome()) {
                Transaction t = this.get(i).clone();
                Money money = new Money(t.getMoney().getCoin(), (t.getMoney().getAmount() * (-1)));
                balance.add(new Transaction(
                        t.id(),
                        t.getCategory(),
                        t.getDate(),
                        money,
                        t.getDescription(),
                        t.isAnIncome(),
                        t.getPhotoUri()
                ));
            } else balance.add(this.get(i));
        } catch (Exception ignored) { }
        return balance;
    }

    /**
     * Sumatoria de todas las transacciones de una misma moneda
     * @param coin Nombre de la moneda a considerar
     * @return Total de transacciones de la moneda definida
     */
    public Money total (final Coin coin) {
        double total = 0;
        if (this.size() > 0) for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getMoney().name().equals(coin.getName()))
                total += this.get(i).getMoney().getAmount();
        }
        return new Money(coin, total);
    }

    /**
     * Sumatoria de todas las transacciones de una misma moneda en una fecha específica
     * @param coin Nombre de la moneda a considerar
     * @param day Fecha de las transacciones
     * @return Total de transacciones de la moneda definida en la fecha definida
     */
    public Money totalToday (final Coin coin, final Date day) {
        double total = 0;
        if (this.size() > 0) for (int i = 0; i < this.size(); i++) {
            if(this.get(i).getMoney().name().equals(coin.getName())
            && this.get(i).getDate().isSameDay(day))
                total += this.get(i).getMoney().getAmount();
        }
        return new Money(coin, total);
    }

    /**
     * Filtra las transacciones de una fecha específica
     * @param day Fecha de las transacciones
     * @return Conjunto de transacciones de una fecha específica
     */
    public Transactions today (final Date day) {
        Transactions transactions = new Transactions();
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getDate().isSameDay(day))
                transactions.add(this.get(i));
        }
        return transactions;
    }

    /**
     * Cantidad de transacciones
     * @return Número de transacciones almacenadas
     */
    public int length() { return this.size(); }

    /**
     * Convierte el conjunto de transacciones en un vector
     * @return Devuelve el conjunto de transacciones en formato vector
     */
    public Transaction[] toList() {
        return this.clone().toArray(new Transaction[0]);
    }

    /**
     * Obtiene un id que no se esté utilizando
     * @return id no utilizado
     */
    public int getUnusedId() { // ids se renuevan cada mes
        if (this.size() > 0) {
            ArrayList<Integer> ids = new ArrayList<>();
            for (int i = 0; i < this.size(); i++)
                ids.add(this.get(i).id());
            return Collections.max(ids) + 1;
        } else return 0;
    }

    @NonNull
    public Transactions clone() {
        Transactions transactions = new Transactions();
        for (Transaction transaction : this) transactions.add(transaction.clone());
        return transactions;
    }

    /**
     * Obtiene el index de la primera aparición de una categoría
     * @param category
     * @return Devuelve el index de la primera aparicion de una categoría o -1 si no existe
     */
    public int indexOf(Category category) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getCategory().getName().equals(category.getName()))
                return i;
        }
        return -1;
    }

    public int indexOf(Transaction transaction) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).equals(transaction))
                return i;
        }
        return -1;
    }

    public void remove(final Transaction transaction) {
        final int index = indexOf(transaction);
        if (index >= 0)
            this.remove(index);
    }

    public void set(final Transaction oldTransaction, final Transaction newTransaction) {
        final int index = indexOf(oldTransaction);
        if (index >= 0)
            this.set(index, newTransaction);
    }

    public boolean exists(Category category) {
        return indexOf(category) != -1;
    }

    //<editor-fold defaultstate="collapsed" desc=" Private functions ">
    /**
     * @return Devuelve todas las transacciones unidas cada una en sus categorías correspondientes,
     *         es decir, no habrá categorías repetidas.
     */
    private ArrayList<Transaction> unifyCategories(ArrayList<Transaction> transactions) {
        ArrayList<Transaction> categories = new ArrayList<>();
        if (transactions.size() > 0) try {
            for (int i = 0; i < this.size(); i++) {
                final Transaction transaction = new Transaction(transactions.get(i).getCategory(), transactions.get(i).getMoney().clone());
                final String categoryName = transaction.getCategory().getName();
                boolean newCategory = true;
                for (int k = 0; k < categories.size(); k++) try {
                    if (categories.get(k).getCategory().getName().equals(categoryName)) {
                        categories.get(k).addMoney(transaction.getMoney().clone());
                        newCategory = false;
                    }
                } catch(Exception e) { return showError("UNIFY ERROR k", e.toString()); }
                if (newCategory) categories.add(transaction);
            }
        } catch (Exception e) { return showError("UNIFY ERROR i", e.toString()); }
        return categories;
    }

    /**
     * @param unorderedCategories Transacciones sin categorías repetidas.
     * @return Devuelve las transacciones sin categorías repetidas ordenadas de mayor a menor.
     */
    private ArrayList<Transaction> sortCategories(final ArrayList<Transaction> unorderedCategories) {

        if (unorderedCategories != null && unorderedCategories.size() > 0) {
            ArrayList<Transaction> categories = new ArrayList<>(unorderedCategories);

            // sort method
            try {
                final int size = categories.size();
                for (int j = 0; j < size - 1; j++) {
                    for (int i = 0; i < size - 1 - j; i++) {
                        if (categories.get(i).getMoney().getAmount() < categories.get(i + 1).getMoney().getAmount()) {
                            Transaction tmp = categories.get(i);
                            categories.set(i, categories.get(i + 1));
                            categories.set(i + 1, tmp);
                        }
                    }
                }
            } catch(Exception e) { return showError("SORT ERROR", e.toString()); }
            return categories;

        }
        return unorderedCategories;
    }

    private ArrayList<Transaction> showError(final String errName, final String e) {
        ArrayList<Transaction> err = new ArrayList<>();
        err.add(new Transaction(new Category(errName + " - " + e), new Money("Dollar", "$", 0)));
        return err;
    }
    //</editor-fold>

}
