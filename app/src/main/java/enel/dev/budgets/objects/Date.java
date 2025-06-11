package enel.dev.budgets.objects;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;

/**
 *  Objeto que representa una fecha en un formato más amigable para poder guardarlo en una base de datos
 *  El formato en que se guardará será YYYYMMDDHH
 */
public class Date {

    private final int day;
    private final int month;
    private final int year;
    private final int hour;

    //<editor-fold defaultstate="collapsed" desc=" Constructor ">
    public Date (final long encodedDate) throws Exception { // YYYYMMDDHH
        final String date = String.valueOf(encodedDate);
        if (date.length() != 10 || encodedDate <= 0) throw new Exception("Invalid length of encoded date");
        this.year = Integer.parseInt(date.substring(0, 4)); // YYYY
        this.month = Integer.parseInt(date.substring(4, 6)); // MM
        this.day = Integer.parseInt(date.substring(6, 8)); // DD
        this.hour = Integer.parseInt(date.substring(8, 10)); // HH
    }

    public Date (final String encodedDate) throws Exception { // YYYYMMDDHH
        this(Integer.parseInt(encodedDate));
    }

    public Date (final int year, final int month, final int day, final int hour) throws Exception {
        if (String.valueOf(year).length() != 4) throw new Exception("Year must be 4 characters");
        if (month < 1 || month > 12) throw new Exception("Month has been between 1 and 12");
        if (!isValidDay(day, month, year)) throw new Exception("Invalid day of month");
        if (hour < 0 || hour > 23) throw new Exception("Hour has been between 0 and 23");
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
    }

    public Date (final int year, final int month, final int day) throws Exception {
        this(year, month, day, 0);
    }

    public Date (final Calendar calendar) {
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
    }

    public Date (final java.util.Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
    }

    public Date () {
        this(new java.util.Date());
    }
    //</editor-fold>

    public String day() { return forceTwoNumbers(this.day); }
    public String month() { return forceTwoNumbers(this.month); }
    public String year() { return forceTwoNumbers(this.year); }
    public String hour() { return forceTwoNumbers(this.hour); }

    public int getDay() { return this.day; }
    public int getMonth() { return this.month; }
    public int getYear() { return this.year; }
    public int getHour() { return this.hour; }

    public boolean isSameDay(final Date date) {
        try {
            if(this.isSameMonth(date) && this.day == Integer.parseInt(date.day()))
                return true;
        } catch(Exception ignored) { }
        return false;
    }

    public boolean isSameMonth(final Date date) {
        try {
            if(this.year == Integer.parseInt(date.year()) && this.month == Integer.parseInt(date.month()))
                return true;
        } catch(Exception ignored) { }
        return false;
    }

    public boolean isSameDay(final java.util.Date date) { return isSameDay(new Date(date)); }
    public boolean isSameMonth(final java.util.Date date) { return isSameMonth(new Date(date)); }

    public boolean isAfter(final Date date) {
        return this.encode() > date.encode();
    }

    public boolean isBefore(final Date date) {
        return this.encode() < date.encode();
    }

    public long encode() { // YYYYMMDDHH
        final String formatedDate =
                forceTwoNumbers(this.year) +
                forceTwoNumbers(this.month) +
                forceTwoNumbers(this.day) +
                forceTwoNumbers(this.hour);
        return Long.parseLong(formatedDate);
    }

    public int partialEncode() { // YYYYMMDD
        final String formatedDate = forceTwoNumbers(this.year) + forceTwoNumbers(this.month) + forceTwoNumbers(this.day);
        return Integer.parseInt(formatedDate);
    }

    /**
     * Obtener la fecha en el formato defindo
     * @param format formato de fecha (MMDDYYYY por defecto)
     * @return Devuelve la fecha en formato String
     */
    public String formatedDate(String format, final boolean onlyMonthAndYear) {
        if (format == null) format = "MMDDYYYY";
        if (format.contains("DM"))
            return onlyMonthAndYear ?
                    forceTwoNumbers(this.month) + "/" + forceTwoNumbers(this.year):
                    forceTwoNumbers(this.day) + "/" + forceTwoNumbers(this.month) + "/" + forceTwoNumbers(this.year);
        else return onlyMonthAndYear ?
                forceTwoNumbers(this.month) + "/" + forceTwoNumbers(this.year):
                forceTwoNumbers(this.month) + "/" + forceTwoNumbers(this.day) + "/" + forceTwoNumbers(this.year);
    }

    /**
     * Obtener la fecha en el formato defindo
     * @param format formato de fecha (MMDDYYYY por defecto)
     * @return Devuelve la fecha en formato String
     */
    public String formatedDate(String format) {
        return formatedDate(format, false);
    }


    /**
     * Obtener la fecha en el formato defindo por defecto (MMDDYYYY)
     * @return Devuelve la fecha en formato String
     */
    public String formatedDate() {
        return formatedDate(null);
    }

    /**
     * Obtener la fecha en el formato extendido (nombre del mes)
     * @return Devuelve la fecha en formato String
     */
    public String formatedDateExtended(final Context activity) {
        return getMonthName(activity) + " " + day() + ", " + getYear();
    }

    /**
     * Obtener la fecha del día de mañana
     * @return
     */
    public Date tomorrow() {
        try {
            if (isValidDay(day + 1, month, year)) return new Date(year, month, day + 1, hour);
            else if (isValidDay(1, month + 1, year)) return new Date(year, month + 1, 1, hour);
            else return new Date(year + 1, 1, 1, hour);
        } catch(Exception ignored) {
            return this;
        }
    }

    /**
     * Obtener la fecha del día de ayer
     * @return
     */
    public Date yesterday() {
        try {
            if (isValidDay(day - 1, month, year)) return new Date(year, month, day - 1, hour);
            else if (isValidDay(maxDay(month - 1, year), month - 1, year)) return new Date(year, month - 1, maxDay(month - 1, year), hour);
            else return new Date(year - 1, 12, maxDay(12, year - 1), hour);
        } catch(Exception ignored) {
            return this;
        }
    }

    /**
     * Obtener la fecha del mes siguiente
     * @return
     */
    public Date nextMonth() {
        try {
            return isValidDay(day, month + 1, year) ?
                    new Date(year, month + 1, day, hour):
                    new Date(year + 1, 1, day, hour);
        } catch(Exception ignored) {
            return this;
        }
    }

    /**
     * Obtener la fecha del mes anterior
     * @return
     */
    public Date lastMonth() {
        try {
            return isValidDay(day, month - 1, year) ?
                    new Date(year, month - 1, day, hour):
                    new Date(year - 1, 12, day, hour);
        } catch(Exception ignored) {
            return this;
        }
    }

    public String getMonthName(final Context activity) {
        switch (this.month) {
            case 1: return activity.getString(R.string.month_1);
            case 2: return activity.getString(R.string.month_2);
            case 3: return activity.getString(R.string.month_3);
            case 4: return activity.getString(R.string.month_4);
            case 5: return activity.getString(R.string.month_5);
            case 6: return activity.getString(R.string.month_6);
            case 7: return activity.getString(R.string.month_7);
            case 8: return activity.getString(R.string.month_8);
            case 9: return activity.getString(R.string.month_9);
            case 10: return activity.getString(R.string.month_10);
            case 11: return activity.getString(R.string.month_11);
            case 12: return activity.getString(R.string.month_12);
        }
        return "";
    }

    public String getMonthNameReduced(final Context activity) {
        switch (this.month) {
            case 1: return activity.getString(R.string.month_1_reduced);
            case 2: return activity.getString(R.string.month_2_reduced);
            case 3: return activity.getString(R.string.month_3_reduced);
            case 4: return activity.getString(R.string.month_4_reduced);
            case 5: return activity.getString(R.string.month_5_reduced);
            case 6: return activity.getString(R.string.month_6_reduced);
            case 7: return activity.getString(R.string.month_7_reduced);
            case 8: return activity.getString(R.string.month_8_reduced);
            case 9: return activity.getString(R.string.month_9_reduced);
            case 10: return activity.getString(R.string.month_10_reduced);
            case 11: return activity.getString(R.string.month_11_reduced);
            case 12: return activity.getString(R.string.month_12_reduced);
        }
        return "";
    }

    public String getMonthNameAndYear(final Context activity) {
        return getMonthName(activity) + " " + getYear();
    }

    public String getMonthNameAndYearReduced(final Context activity) {
        return getMonthNameReduced(activity) + " " + getYear();
    }

    private static String forceTwoNumbers(final int n) {
        String date = String.valueOf(n);
        return date.length() == 1 ? "0" + date : date;
    }

    private static int maxDay(final int month, final int year) {
        switch (month) {
            case 11: return 30;
            case 4: return 30;
            case 6: return 30;
            case 9: return 30;
            case 2: return (year % 4 == 0) ? 29 : 28; // February
            default: return 31;
        }
    }

    private static boolean isValidDay(final int day, final int month, final int year) {
        if (day < 1 || month < 1 || month > 12) return false;
        return day <= maxDay(month, year);
        /*switch (month) {
            case 11: return day < 31; // Noviembre
            case 4: return day < 31; // Abril
            case 6: return day < 31; // Junio
            case 9: return day < 31; // Septiembre
            case 2: { // Febrero
                if (year % 4 == 0) return day < 30;
                else return day < 29;
            }
            default: return day < 32;
        }*/
    }

    @Override
    public String toString() {
        return String.valueOf(encode());
    }

    public long toLong() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, this.getYear());  // Año
        calendar.set(Calendar.MONTH, this.getMonth() - 1);  // Mes (recuerda que los meses son indexados desde 0)
        calendar.set(Calendar.DAY_OF_MONTH, this.getDay());  // Día del mes
        return calendar.getTimeInMillis();
    }

}
