package enel.dev.budgets.data.sql.external.reservations;

import enel.dev.budgets.objects.reserve.Reserve;

class Body {
    public static Body create(final Reserve reserve, final int id) {
        return new Body(
                id,
                reserve.getName(),
                reserve.getAmount()
        );
    }

    public static Body create(final Reserve reserve) {
        return create(reserve, reserve.id());
    }

    public static Reserve recreate(final Reserve reserve, final int id) {
        if (reserve != null) {
            return new Reserve(
                    id,
                    reserve.getName(),
                    reserve.getAmount()
            );
        } else return null;
    }

    public int id;
    public String name;
    public double amount; // Cantidad transaccionada

    public Body(final int id, final String name, final double amount) {

        this.id = id;
        this.name = name;
        this.amount = amount;

    }
}
