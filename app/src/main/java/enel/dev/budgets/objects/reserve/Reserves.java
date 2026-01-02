package enel.dev.budgets.objects.reserve;

import java.util.ArrayList;
import java.util.Collections;


public class Reserves extends ArrayList<Reserve> {

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

}
