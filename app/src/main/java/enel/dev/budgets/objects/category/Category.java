package enel.dev.budgets.objects.category;

import androidx.annotation.NonNull;

/**
 *  Un objeto que representará una categoría de transacción
 *  Contendrá un nombre, un id de imagen y un id de color para la representación gráfica
 */
public class Category {

    private final String name;
    private final int imageId;
    private final int colorId;
    private final boolean isAnIncome;

    public Category(final String name, final int imageId, final int colorId, final boolean isAnIncome) {
        this.name = name;
        this.imageId = imageId;
        this.colorId = colorId;
        this.isAnIncome = isAnIncome;
    }

    public Category(final String name) {
        this(name, 119, 0, false);
    }

    public Category(final String name, final int imageId, final int colorId) {
        this(name, imageId, colorId, false);
    }

    public String getName() { return this.name; }
    public int getImage() { return Icon.icon(imageId); }
    public int getImageId() { return this.imageId; }

    public int getColor() { return Color.color(colorId); }
    public int getColorId() { return this.colorId; }

    public boolean isAnIncome() {
        return this.isAnIncome;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }

    public Category clone() {
        return new Category(this.name, this.imageId, this.colorId, this.isAnIncome);
    }

    public String serialize() {
        final String name = this.name;
        final String image = String.valueOf(this.imageId);
        final String color = String.valueOf(this.colorId);
        final String isAnIncome = this.isAnIncome ? "true" : "false";

        return "{category:" + name + "," +
                "image:" + image + "," +
                "color:" + color + "," +
                "isanincome:" + isAnIncome + "}";
    }

    public boolean equals(Category obj) {
        return this.name.equals(obj.getName())
                && this.isAnIncome == obj.isAnIncome()
                && this.imageId == obj.getImageId()
                && this.colorId == obj.getColorId();
    }

    public static Category newInstance(final String serializedMoney) {
        if (serializedMoney != null && serializedMoney.charAt(0) == '{' && serializedMoney.charAt(serializedMoney.length()-1) == '}') {
            final String json = serializedMoney.substring(1, serializedMoney.length() - 1);
            final String[] objects = json.split(",");
            if (objects.length == 4) try {
                final String name = objects[0].substring(objects[0].indexOf(':')+1);
                final int image = Integer.parseInt(objects[1].substring(objects[1].indexOf(':')+1));
                final int color = Integer.parseInt(objects[2].substring(objects[2].indexOf(':')+1));
                final boolean income = objects[2].substring(objects[2].indexOf(':')+1).equals("true");
                return new Category(name, image, color, income);
            } catch (Exception ignored) { }
        } return null;
    }

}
