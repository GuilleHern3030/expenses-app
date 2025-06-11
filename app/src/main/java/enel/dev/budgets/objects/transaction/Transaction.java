package enel.dev.budgets.objects.transaction;

import android.util.Log;

import androidx.annotation.NonNull;

import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.category.Category;
import enel.dev.budgets.objects.money.Money;

/**
 *  Un objeto que representa una transacción
 *  Contiene una fecha, una cantidad de dinero, una descripción y una categoría que lo represente
 *  Además, contiene el id donde se almacena en la base de datos
 */
public class Transaction {

    private final static int MAX_CHARACTERS = 130;

    private final Date date; // Fecha de la transaccion (YYYYMMDDHH)
    private Money money; // Cantidad transaccionada
    private String description; // Descripcion de la transaccion
    private Category category; // Categoría de la transaccion
    private final boolean isAnIncome; // ¿Es un ingreso?
    private String photoUri; // Foto asignada
    private final int id;

    public Transaction(
            final int id,
            final Category category,
            final Date date,
            final Money money,
            final String description,
            final boolean isAnIncome,
            final String photoUri)
    {
        this.id = id;
        this.category = category != null ? category : new Category("Default category", 0, 0);
        this.date = date != null ? date : new Date();
        this.money = money != null ? money : new Money("", "", 0);
        this.description = description != null ? description : "";
        this.isAnIncome = isAnIncome;
        this.photoUri = photoUri;
    }

    public Transaction(final int id, final Category category, final Date date, final Money money, final String description, final boolean isAnIncome) {
        this(id, category, date, money, description, isAnIncome, null);
    }

    public Transaction(final int id, final Category category, final Date date, final Money money, final String description) {
        this(id, category, date, money, description, false, null);
    }

    public Transaction(final Category category, final Money money) {
        this(-1, category, null, money, null, false, null);
    }

    public int id() { return this.id; }

    public Date getDate() { return this.date; }

    public Money getMoney() { return this.money; }

    public boolean isAnIncome() { return this.isAnIncome; }

    public String getDescription() { return this.description != null ? this.description : ""; }

    public Category getCategory() { return this.category; }

    public String getPhotoUri() { return this.photoUri != null ? this.photoUri : ""; }
    public String getPhotoFileName() { return String.valueOf(this.date.encode()) + String.valueOf(this.id); }
    public void setPhotoUri(final String uri) { this.photoUri = uri; }

    public void setDescription(final String description) { this.description = description; }

    public void setCategory(final Category category) { this.category = category; }

    public void setMoney(final Money money) { this.money = money; }
    public void setMoney(final int amount) { this.money.setAmount(amount); }
    public void setMoney(final double amount) { this.money.setAmount(amount); }
    public void addMoney(final double amount) { this.money.setAmount(this.money.getAmount() + amount); }
    public void addMoney(final Money money) { this.money.setAmount(this.money.getAmount() + money.getAmount()); }

    public Transaction clone() {
        return new Transaction(
                this.id(),
                this.getCategory(),
                this.getDate(),
                this.getMoney().clone(),
                this.getDescription(),
                this.isAnIncome(),
                this.getPhotoUri()
        );
    }

    public boolean equals(final Transaction transaction) {
        return this.date.encode() == transaction.getDate().encode() && this.id == transaction.id();
    }

    public String getDescriptionReduced() {
        String descriptionReduced = this.description;

        if (descriptionReduced.length() > MAX_CHARACTERS)
            descriptionReduced = descriptionReduced.substring(0, MAX_CHARACTERS);

        final int newLineIndex = descriptionReduced.contains("\n") ? descriptionReduced.indexOf("\n") : descriptionReduced.indexOf("\n\n");
        if (newLineIndex > -1)
            descriptionReduced = descriptionReduced.substring(0, newLineIndex);

        return descriptionReduced;
    }

    public String serialize() {
        String category = this.category != null ? this.category.serialize() : "null";
        String date = this.date != null ? this.date.toString() : "null";
        String money = this.money != null ? this.money.serialize() : "null";
        String isAnIncome = this.isAnIncome ? "true" : "false";

        return "{id:" + id + ";" +
                "category:" + category + ";" +
                "date:" + date + ";" +
                "money:" + money + ";" +
                "description:" + description + ";" +
                "isAnIncome:" + isAnIncome + ";" +
                "photoUri:" + photoUri + "}";
    }

    public static Transaction newInstance(final String serializedTransaction) {
        if (serializedTransaction != null && serializedTransaction.charAt(0) == '{' && serializedTransaction.charAt(serializedTransaction.length()-1) == '}') {
            final String json = serializedTransaction.substring(1, serializedTransaction.length() - 1);
            final String[] objects = json.split(";");
            if (objects.length == 7) try {
                final int id = Integer.parseInt(objects[0].substring(objects[0].indexOf(':')+1));
                final Category category = Category.newInstance(objects[1].substring(objects[1].indexOf(':')+1));
                final Date date = new Date(objects[2].substring(objects[2].indexOf(':')+1));
                final Money money = Money.newInstance(objects[3].substring(objects[3].indexOf(':')+1));
                final String description = objects[4].substring(objects[4].indexOf(':')+1);
                final boolean isAnIncome = objects[5].substring(objects[5].indexOf(':')+1).equals("true");
                final String photoUri = objects[6].substring(objects[6].indexOf(':')+1);
                return new Transaction(id, category, date, money, description, isAnIncome, photoUri.equals("null") ? null : photoUri);
            } catch (Exception e) {
                Log.e("TRANSACTION_NEW_INSTANCE", serializedTransaction);
                Log.e("TRANSACTION_NEW_INSTANCE", "newInstance: ",e);
            }
        } return null;
    }

    @NonNull
    public String toString() {
        return "[ " + this.date.toString() + "] (" + this.category.getName() + ") {" + this.id + "} " + this.money.toString();
    }
}
