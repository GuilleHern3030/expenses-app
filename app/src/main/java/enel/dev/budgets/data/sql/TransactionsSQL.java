package enel.dev.budgets.data.sql;

import static enel.dev.budgets.data.sql.Controller.DATA_BASE;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.category.Categories;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.money.Coin;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;

public class TransactionsSQL {

    private final Context context;

    public TransactionsSQL(final Context context) {
        this.context = context;
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
     * @param year
     * @param month
     * @return
     */
    public Transactions get(final int year, final int month) {
        final Transactions transactions = new Transactions();
        final String tableName = transactionsTableName(year, month);
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final int rows = sql.tablaFilas(tableName);
        final Categories categories = CategoriesSQL.getCategories(sql);
        if (rows > 0) for (int row = 0; row < rows; row++) try {
            final String[] col = sql.tablaObtenerFila(tableName, row);
            final Category category = categories.getCategory(col[5]);
            transactions.add(new Transaction(
                    Integer.parseInt(col[0]), // id
                    category != null ? category : new Category(context.getString(R.string.default_category)), // category
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

    /**
     * Obtener todas las transacciones de un mes específico
     * @param date
     * @return
     */
    public Transactions get(final Date date) {
        return get(date.getYear(), date.getMonth());
    }

    /**
     * Obtener todas las transacciones de un período de tiempo específico
     * @param initDate
     * @param endDate
     * @return
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

    /**
     * Obtener todas las transacciones del mes corriente
     * @return
     */
    public Transactions get() {
        final Date currentDate = new Date();
        return get(currentDate.getYear(), currentDate.getMonth());
    }

    public boolean add(final Transaction transaction) {
        if (transaction == null || transaction.id() == -1) return false;
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
        return newRow >= 0;
    }

    public boolean edit(final int id, final Transaction transaction) {
        if (transaction == null) return false;
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        final String tableName = transactionsTableName(transaction.getDate().getYear(), transaction.getDate().getMonth());
        if (sql.tablaExiste(tableName)) {
            final int row = sql.tablaBuscarFila(tableName, "id", String.valueOf(id), true);
            if (row >= 0) {
                return sql.tablaEditarFila(tableName, row, new String[]{
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
        }
        sql.cerrar();
        return false;
    }

    public boolean delete(final int id, final String dateEncoded) {
        if (dateEncoded == null || id < 0) return false;
        final BasicSQL sql = new BasicSQL(context, DATA_BASE);
        try {
            final Date date = new Date(dateEncoded);
            final String tableName = transactionsTableName(date.getYear(), date.getMonth());
            if (sql.tablaExiste(tableName)) {
                final int row = sql.tablaBuscarFila(tableName, "id", String.valueOf(id), false);
                if (row >= 0)
                    return sql.tablaEliminarFila(tableName, row, true);
            }
        } catch(Exception ignored) { }
        sql.cerrar();
        return false;
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

}
