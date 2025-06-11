package enel.dev.budgets.objects.category;

import enel.dev.budgets.R;


public class Color {

    public static int[] colors() {
        return new int[] {
                R.drawable.icons_border_gray_light,
                R.drawable.icons_border_gray,
                R.drawable.icons_border_gray_dark,
                R.drawable.icons_border_pink_light,
                R.drawable.icons_border_pink,
                R.drawable.icons_border_pink_dark,
                R.drawable.icons_border_red_light,
                R.drawable.icons_border_red,
                R.drawable.icons_border_red_dark,
                R.drawable.icons_border_brown_light,
                R.drawable.icons_border_brown,
                R.drawable.icons_border_brown_dark,
                R.drawable.icons_border_orange_light,
                R.drawable.icons_border_orange,
                R.drawable.icons_border_orange_dark,
                R.drawable.icons_border_yellow_light,
                R.drawable.icons_border_yellow,
                R.drawable.icons_border_yellow_dark,
                R.drawable.icons_border_green_light,
                R.drawable.icons_border_green,
                R.drawable.icons_border_green_dark,
                R.drawable.icons_border_blue_light,
                R.drawable.icons_border_blue,
                R.drawable.icons_border_blue_dark,
                R.drawable.icons_border_violet_light,
                R.drawable.icons_border_violet,
                R.drawable.icons_border_violet_dark,
                R.drawable.icons_border_indigo_light,
                R.drawable.icons_border_indigo,
                R.drawable.icons_border_indigo_dark
        };
    }

    public static int color(final int id) {
        try {
            return colors()[id];
        } catch(Exception ignored) {
            return colors()[0];
        }
    }

}
