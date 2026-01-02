package enel.dev.budgets.data.sql.local;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;

import enel.dev.budgets.data.sql.BasicSQL;
import enel.dev.budgets.data.sql.Controller;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;

public class TransactionsSQL extends enel.dev.budgets.data.sql.TransactionsSQL {

    public TransactionsSQL(final Context context, final String dbName) {
        super(context, dbName);
    }

    private static void createTransactionsTable(@NonNull BasicSQL sql, @NonNull Date date) {
        sql.tablaCrear(transactionsTableName(date.getYear(), date.getMonth()), new String[]{
                "id",
                "coinname",
                "coinsymbol",
                "amount",
                "date",
                "category",
                "isanincome",
                "description",
                "photouri"
        });
    }

    /**
     * Obtiene el nombre de la tabla según el mes y año en que se busca.
     * Las columnas son:
     *          0- id
     *          1- coinname: nombre de la moneda
     *          2- coinsymbol: simbolo de la moneda
     *          3- amount: cantidad de dinero (double)
     *          4- date (encoded)
     *          5- category: nombre de la categoría
     *          6- isincome: es un 'income'? (1 para sí)
     *          7- description: descripcion de la transaccion
     *          8- photouri: uri de la foto relacionada
     * @param year año
     * @param month (1 es Enero, 12 es Diciembre)
     * @return Devuelve el nombre tal como se guarda en la base de datos, según el año y mes.
     */
    private static String transactionsTableName(final int year, final int month) {
        return "MONTH_" + month + "_" + year;
    }

    /**
     * Obtener todas las transacciones de un mes específico
     * @param year Año
     * @param month Mes
     * @return Conjunto de transacciones
     */
    public Transactions get(final int year, final int month) {
        final String tableName = transactionsTableName(year, month);
        return get(tableName);
    }

    /**
     * Obtener todas las transacciones de un mes específico
     * @param tableName Nombre de la tabla específica
     * @return Conjunto de transacciones
     */
    public Transactions get(final String tableName) {
        final Transactions transactions = new Transactions();
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final int rows = sql.tablaFilas(tableName);
        final Categories categories = CategoriesSQL.getCategories(sql);
        if (rows > 0) for (int row = 0; row < rows; row++) try {
            final String[] col = sql.tablaObtenerFila(tableName, row);
            final Category category = categories.getCategory(col[5]);
            transactions.add(new Transaction(
                    Integer.parseInt(col[0]), // id
                    category != null ? category : new Category(), // category
                    new Date(col[4]), // date
                    new Money(col[1], col[2], Double.parseDouble(col[3])), // money
                    col[7], // description
                    Integer.parseInt(col[6]) == 1, // is an income?
                    col[8] // photouri
            ));
        } catch(Exception ignored) { }
        sql.cerrar();
        return transactions;
    }

    @Override
    public void get (final int year, final int month, final TransactionsCallback callback) {
        final Transactions transactions = get(year, month);
        callback.onSuccess(transactions);
    }

    @Override
    public void get(@NonNull Date initDate, @NonNull Date endDate, TransactionsCallback callback) {
        final Transactions transactions = get(initDate, endDate);
        callback.onSuccess(transactions);
    }

    /**
     * Obtener todas las transacciones de un mes específico
     * @param date Fecha
     */
    public Transactions get(final Date date) {
        return get(date.getYear(), date.getMonth());
    }

    /**
     * Obtener todas las transacciones del mes corriente
     */
    public Transactions get() {
        final Date date = new Date();
        return get(date.getYear(), date.getMonth());
    }

    /**
     * Obtener todas las transacciones de un período de tiempo específico
     * @param initDate Fecha inicial
     * @param endDate Fecha final
     */
    public Transactions get(@NonNull final Date initDate, @NonNull final Date endDate) {
        final Transactions transactions = new Transactions();
        if (initDate.isAfter(endDate)) return transactions;
        Date date = initDate;
        while (date.encode() < endDate.encode()) try {
            Transactions tmpTransactions = get(date.getYear(), date.getMonth());
            for (Transaction transaction : tmpTransactions)
                if (transaction.getDate().encode() < endDate.encode() && transaction.getDate().encode() > initDate.encode())
                    transactions.add(transaction);
            date = date.nextMonth();
        } catch (Exception e) { Log.e("CONTROLLER", "Controller.getTransactions error", e); }
        return transactions;
    }

    @Override
    public void add(final Transaction transactionToAdd, final TransactionCallback callback) {
        final Transaction transaction = transaction(transactionToAdd);
        if (transactionToAdd == null || transaction.id() == -1) { callback.onError("Transaction null"); return; }
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final String tableName = transactionsTableName(transaction.getDate().getYear(), transaction.getDate().getMonth());
        if (!sql.tablaExiste(tableName))
            createTransactionsTable(sql, transaction.getDate());
        String[] data = new String[]{
                String.valueOf(transaction.id()), // ID
                transaction.getMoney().name(), // coin name
                transaction.getMoney().getCoin().getSymbol(), // coin symbol
                String.valueOf(transaction.getMoney().getAmount()), // amount
                transaction.getDate().toString(), // date encoded
                transaction.getCategory().getName(), // category name
                transaction.isAnIncome() ? "1" : "0", // isAnIncome
                transaction.getDescription(), // description
                transaction.getPhotoUri()
        };
        final int newRow = sql.tablaIngresarFila(tableName, data);
        sql.cerrar();
        if (newRow >= 0) callback.onSuccess(transaction);
        else callback.onError("");
    }

    /**
     * Edita la información de una transacción sin modificar el mes de la transacción
     * @param oldTransaction Transacción con la información anterior
     * @param newTransaction Transacción con la nueva información
     */
    @Override
    public void edit(final Transaction oldTransaction, final Transaction newTransaction, final TransactionCallback callback) {
        if (oldTransaction == null || newTransaction == null) { callback.onError("Transaction null"); return; }

        Transaction transactionToEdit;
        if (!oldTransaction.getDate().isSameMonth(newTransaction.getDate())) {
            final int newId = move(newTransaction, oldTransaction.getDate(), newTransaction.getDate());
            if (newId >= 0)
                transactionToEdit = new Transaction(newId, newTransaction.getCategory(), newTransaction.getDate(), newTransaction.getMoney(), newTransaction.getDescription(), newTransaction.isAnIncome(), newTransaction.getPhotoUri()); //transaction(newId);
            else throw new IndexOutOfBoundsException();
        } else transactionToEdit = newTransaction;

        final Transaction transaction = transaction(transactionToEdit);
        if (transaction.id() == -1) { callback.onError("Transaction invalid"); return; }
        boolean success = false;
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final String tableName = transactionsTableName(transaction.getDate().getYear(), transaction.getDate().getMonth());
        if (sql.tablaExiste(tableName)) {
            final int row = sql.tablaBuscarFila(tableName, "id", String.valueOf(transaction.id()), true);
            if (row >= 0)
                success = sql.tablaEditarFila(tableName, row, new String[]{
                        String.valueOf(transaction.id()), // ID
                        transaction.getMoney().name(), // coin name
                        transaction.getMoney().getCoin().getSymbol(), // coin symbol
                        String.valueOf(transaction.getMoney().getAmount()), // amount
                        transaction.getDate().toString(), // date encoded
                        transaction.getCategory().getName(), // category name
                        transaction.isAnIncome() ? "1" : "0", // isAnIncome
                        transaction.getDescription(), // description
                        transaction.getPhotoUri()
                });
        }
        sql.cerrar();
        if (success) callback.onSuccess(transaction);
        else callback.onError("");
    }

    /**
     * Mueve una transacción a una nueva fecha, lo cual provocará un cambio en el ID de la transacción
     * @param transaction Transacción que se quiere mover (no se modificará ninguno de sus datos)
     * @param oldDate Fecha donde se encuentra la transacción actualmente
     * @param newDate Fecha a donde se moverá la transacción sin modificar sus datos internos
     * @return nuevo ID de la transacción
     */
    private int move(final Transaction transaction, final Date oldDate, final Date newDate) {
        final int unusedId = getUnusedId(newDate);
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        try {
            final String oldTableName = transactionsTableName(oldDate.getYear(), oldDate.getMonth());
            final String newTableName = transactionsTableName(transaction.getDate().getYear(), transaction.getDate().getMonth());
            if (sql.tablaExiste(oldTableName)) {
                final int oldRow = sql.tablaBuscarFila(oldTableName, "id", String.valueOf(transaction.id()), true);
                if (oldRow >= 0) sql.tablaEliminarFila(oldTableName, oldRow, true);
                else throw new Exception();
            }
            if (!sql.tablaExiste(newTableName))
                createTransactionsTable(sql, transaction.getDate());
            sql.tablaIngresarFila(newTableName, new String[]{
                    String.valueOf(unusedId), // ID
                    transaction.getMoney().name(), // coin name
                    transaction.getMoney().getCoin().getSymbol(), // coin symbol
                    String.valueOf(transaction.getMoney().getAmount()), // amount
                    transaction.getDate().toString(), // date encoded
                    transaction.getCategory().getName(), // category name
                    transaction.isAnIncome() ? "1" : "0", // isAnIncome
                    transaction.getDescription(), // description
                    transaction.getPhotoUri()
            });
            return unusedId;
        } catch(Exception ignored) { }
        sql.cerrar();
        return -1;
    }

    private int getUnusedId(final Date date) {
        Transactions transactions = get(date.getYear(), date.getMonth());
        return transactions.getUnusedId();
    }

    @Override
    public void delete(final int id, final String dateEncoded, final Controller.SQLcallback callback) {
        if (dateEncoded == null || id < 0) { callback.onError("Transaction null"); return; }
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        try {
            final Date date = new Date(dateEncoded);
            final String tableName = transactionsTableName(date.getYear(), date.getMonth());
            if (sql.tablaExiste(tableName)) {
                final int row = sql.tablaBuscarFila(tableName, "id", String.valueOf(id), false);
                if (row >= 0) {
                    final boolean result = sql.tablaEliminarFila(tableName, row, true);
                    if (result)
                        callback.onSuccess();
                    else callback.onError("Delete error");
                    return;
                }
            }
        } catch(Exception ignored) { }
        sql.cerrar();
        callback.onError("Delete error");
    }

    private String[] transactionsTables(final BasicSQL sql) {
        final String[] tables = sql.listarTablas();
        final ArrayList<String> monthsArrayList = new ArrayList<>();
        for (String table : tables)
            if (table.startsWith("MONTH"))
                monthsArrayList.add(table);
        final String[] months = new String[monthsArrayList.size()];
        for (int i = 0; i < monthsArrayList.size(); i++)
            months[i] = monthsArrayList.get(i);
        return months;
    }

    @Override
    public void deleteCoin(final Coin coin) {
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final String[] months = transactionsTables(sql);
        Log.d("TRANSACTIONS_TABLES", Arrays.toString(months));
        if (coin != null) try {
            for (String month : months) {
                final int[] rows = sql.tablaBuscarFilas(month, "coinname", coin.getName(), false);
                for (int row : rows) sql.tablaEliminarFila(month, row, false);
            }
        } catch (Exception e) { Log.e(getClass().getName(), "deleteCoin: ", e); }
        sql.cerrar();
    }

    @Override
    public void editCoin(final Coin oldCoin, final Coin newCoin) {
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final String[] months = transactionsTables(sql);
        Log.d("TRANSACTIONS_TABLES", Arrays.toString(months));
        if (oldCoin != null && newCoin != null) try {
            for (String month : months) {
                final int[] rows = sql.tablaBuscarFilas(month, "coinname", oldCoin.getName(), false);
                for (int row : rows) {
                    final String[] data = sql.tablaObtenerFila(month, row);
                    sql.tablaEditarFila(month, row, new String[]{
                            data[0], // id
                            newCoin.getName(),
                            newCoin.getSymbol(),
                            data[3], // amount
                            data[4], // date
                            data[5], // category
                            data[6], // isincome
                            data[7], // description
                            data[8]  // photouri
                    });
                }
            }
        } catch (Exception e) { Log.e(getClass().getName(), "deleteCoin: ", e); }
        sql.cerrar();
    }

    private Transaction transaction(final Transaction transaction) {
        if (transaction != null) {
            final int finalId = transaction.id() != -1 ? transaction.id() : get(transaction.getDate()).getUnusedId();
            return new Transaction(
                    finalId,
                    transaction.getCategory(),
                    transaction.getDate(),
                    transaction.getMoney(),
                    transaction.getDescription(),
                    transaction.isAnIncome(),
                    transaction.getPhotoUri()
            );
        } else return null;
    }

}
