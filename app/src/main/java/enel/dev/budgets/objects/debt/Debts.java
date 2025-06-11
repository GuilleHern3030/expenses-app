package enel.dev.budgets.objects.debt;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class Debts extends ArrayList<Debt> {


    /**
     * Obtiene un id que no se esté utilizando
     * @return id no utilizado
     */
    public int getUnusedId() {
        if (this.size() > 0) {
            ArrayList<Integer> ids = new ArrayList<>();
            for (int i = 0; i < this.size(); i++)
                ids.add(this.get(i).id());
            return Collections.max(ids) + 1;
        } else return 0;
    }

    /**
     * Obtiene todas las deudas de la moneda definida
     * @return Devuelve un conjunto de deudas de la moneda seleccionada
     */
    public Debts filterCoin(final String coinName) {
        Debts debts = new Debts();
        for (int i = 0; i < this.size(); i++) try {
            if (this.get(i).getMoney().getCoin().getName().equals(coinName))
                debts.add(this.get(i));
        } catch (Exception e) {
            Log.e("DEBTS", "filterCoin: ", e);
        }
        return debts;
    }

}
