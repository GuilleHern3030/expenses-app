package enel.dev.budgets.data.sql;

import android.content.Context;

import androidx.annotation.NonNull;

import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;

public abstract class TransactionsSQL {

    protected final Context context;
    protected final String DATA_BASE;

    public TransactionsSQL(final Context context, final String dbName) {
        this.context = context;
        this.DATA_BASE = dbName;
    }

    public interface TransactionCallback {
        void onSuccess(final Transaction transactionResult);
        void onError(final String error);
        void onNetworkError();
    }

    public interface TransactionsCallback {
        void onSuccess(final Transactions transactionResult);
        void onFailure(final int errorCode);
    }

    /**
     * Obtener todas las transacciones de un mes específico
     * @param year Año
     * @param month Mes
     */
    public abstract void get(final int year, final int month, final TransactionsCallback callback);

    /**
     * Obtener todas las transacciones de un mes específico
     * @param date Fecha
     */
    public void get(final Date date, final TransactionsCallback callback) {
        get(date.getYear(), date.getMonth(), callback);
    }

    /**
     * Obtener todas las transacciones del mes corriente
     */
    public void get(final TransactionsCallback callback) {
        final Date currentDate = new Date();
        get(currentDate.getYear(), currentDate.getMonth(), callback);
    }

    /**
     * Obtener todas las transacciones de un período de tiempo específico
     * @param initDate Fecha inicial
     * @param endDate Fecha final
     */
    public abstract void get(@NonNull final Date initDate, @NonNull final Date endDate, final TransactionsCallback callback);

    public abstract void add(final Transaction transaction, final TransactionCallback callback);

    /**
     * Edita la información de una transacción sin modificar el mes de la transacción
     * @param transaction Transacción con la nueva información
     */
    public abstract void edit(final Transaction oldTransaction, final Transaction transaction, final TransactionCallback callback);

    public abstract void delete(final int id, final String dateEncoded, final Controller.SQLcallback callback);

    public abstract void deleteCoin(final Coin coin);

    public abstract void editCoin(final Coin oldCoin, final Coin newCoin);

}
