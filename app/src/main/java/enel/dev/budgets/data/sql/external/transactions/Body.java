package enel.dev.budgets.data.sql.external.transactions;

import java.util.ArrayList;

import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;

class Body {

    public static Body create(final Transaction transaction, final int id) {
        return new Body(
                id,
                transaction.getDate().toString(),
                transaction.getDescription(),
                transaction.isAnIncome(),
                transaction.getCategory().getName(),
                transaction.getCategory().getImageId(),
                transaction.getCategory().getColorId(),
                transaction.getMoney().getCoin().getName(),
                transaction.getMoney().getCoin().getSymbol(),
                transaction.getMoney().getAmount()
        );
    }

    public static Body create(final Transaction transaction) {
        return create(transaction, transaction.id());
    }

    public static Transaction recreate(final Transaction transaction, final int id) {
        if (transaction != null) {
            return new Transaction(
                    id,
                    transaction.getCategory(),
                    transaction.getDate(),
                    transaction.getMoney(),
                    transaction.getDescription(),
                    transaction.isAnIncome(),
                    transaction.getPhotoUri()
            );
        } else return null;
    }

    public static Transactions recreate(final Body[] transactions) throws Exception {
        Transactions transactionsArray = new Transactions();
        if (transactions != null) {
            for (Body transaction : transactions) {
                transactionsArray.add(new Transaction(
                        transaction.id,
                        new Category(transaction.category_name, transaction.category_imageId, transaction.category_colorId, transaction.isAnIncome),
                        new Date(transaction.date),
                        new Money(transaction.coin_name, transaction.coin_symbol, transaction.amount),
                        transaction.description,
                        transaction.isAnIncome
                ));
            }
        }
        return transactionsArray;
    }

    public int id;
    public String date; // Fecha de la transaccion (YYYYMMDDHH)
    public String description; // Descripcion de la transaccion
    public boolean isAnIncome; // ¿Es un ingreso?

    // Category
    public String category_name;
    public int category_imageId;
    public int category_colorId;

    // Money
    public String coin_name; // Cantidad transaccionada (Objeto Coin)
    public String coin_symbol; // Cantidad transaccionada (Objeto Coin)
    public double amount; // Cantidad transaccionada

    public Body(
            final int id, final String date, final String description, final boolean isAnIncome,
            final String category_name, final int category_imageId, final int category_colorId,
            final String coin_name, final String coin_symbol, final double amount) {

        this.id = id;
        this.date = date;
        this.description = description;
        this.isAnIncome = isAnIncome;
        this.category_name = category_name;
        this.category_imageId = category_imageId;
        this.category_colorId = category_colorId;
        this.coin_name = coin_name;
        this.coin_symbol = coin_symbol;
        this.amount = amount;

    }

}
