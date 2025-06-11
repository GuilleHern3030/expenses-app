package enel.dev.budgets.objects.transaction;

import java.util.ArrayList;
import java.util.Comparator;

public class TransactionsArray extends ArrayList<Transactions> {

    public TransactionsArray(final Transactions transactions) {

        if (transactions != null && transactions.size() > 0) {
            Transactions array = new Transactions();
            array.addAll(transactions);
            array.sort(Comparator.comparingLong(o -> o.getDate().encode())); // ordenar por fecha

            int currentDate = 0;
            for (int i = 0; i < transactions.size(); i++) {
                final Transaction transaction = array.get(i);
                final int date = transaction.getDate().partialEncode(); // YYYYMMDD
                if (date != currentDate) {
                    currentDate = date;
                    this.add(new Transactions());
                }
                this.get(this.size() - 1).add(transaction);
            }
        }
    }

    /**
     * Convierte un ArrayList de transacciones en un vector
     * @return Devuelve el mismo ArrayList convertido en un vector
     */
    public Transactions[] toList() {
        return this.toArray(new Transactions[0]);
    }

    public int length() {
        return this.size();
    }
}
