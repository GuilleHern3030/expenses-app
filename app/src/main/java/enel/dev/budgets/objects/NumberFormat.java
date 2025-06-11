package enel.dev.budgets.objects;

import androidx.annotation.NonNull;

import java.util.Locale;

import enel.dev.budgets.data.preferences.Preferences;

public enum NumberFormat {
    DOT_COMMA(';'), // 123.456,78
    COMMA_DOT('!'), // 123,456.78
    NONE_DOT('.'), // 123456.78
    NONE_COMMA(','), // 123456,78
    DOT_NONE('-'), // 123.457 (no decimals)
    COMMA_NONE('_'), // 123,457 (no decimals)
    NONE_NONE('*'); // 123457 (no decimals)

    public static NumberFormat newInstance(final char decimalFormatChar) {
        switch (decimalFormatChar) {
            case ';': return NumberFormat.DOT_COMMA;
            case '.': return NumberFormat.NONE_DOT;
            case ',': return NumberFormat.NONE_COMMA;
            case '-': return NumberFormat.DOT_NONE;
            case '_': return NumberFormat.COMMA_NONE;
            case '*': return NumberFormat.NONE_NONE;
            default: return NumberFormat.COMMA_DOT;
        }
    }

    public static NumberFormat newInstance(final int ordinal) {
        switch (ordinal) {
            case 0: return NumberFormat.DOT_COMMA;
            case 2: return NumberFormat.NONE_DOT;
            case 3: return NumberFormat.NONE_COMMA;
            case 4: return NumberFormat.DOT_NONE;
            case 5: return NumberFormat.COMMA_NONE;
            case 6: return NumberFormat.NONE_NONE;
            default: return NumberFormat.COMMA_DOT;
        }
    }

    private final char decimalFormat;

    NumberFormat(char decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    public char get() {
        return this.decimalFormat;
    }

    public char toChar() {
        return this.decimalFormat;
    }

    @NonNull
    public String toString(final double amount) {
        String value = String.format(Locale.US, "%.2f", amount >= 0 ? amount : -amount);
        StringBuilder valueFixed = new StringBuilder();
        final char decimalSeparator = decimalSeparator();
        boolean decimalEnded = false;
        int count = 0;
        for(int i = value.length(); i > 0; i--) {
            if (!decimalEnded) {
                if (value.charAt(i - 1) == '.') {
                    decimalEnded = true;
                    valueFixed.append(decimalSeparator);
                } else valueFixed.append(value.charAt(i - 1));
            } else {
                if (count == 3 && withCentenals()) {
                    valueFixed.append(centenalSeparator());
                    count = 0;
                }
                valueFixed.append(value.charAt(i - 1));
                count ++;
            }
        }
        if (amount < 0) valueFixed.append("-");
        String result = valueFixed.reverse().toString();
        if (!withDecimals())
            result = result.substring(0, result.length() - Preferences.DEFAULT_DECIMALS_AMOUNT - 1);
        return result;
    }

    public char decimalSeparator() {
        switch (this) {
            case DOT_COMMA:
            case NONE_COMMA:
                return ',';
            default:
                return '.';
        }
    }

    public char centenalSeparator() {
        switch (this) {
            case COMMA_NONE:
            case COMMA_DOT:
                return ',';
            default:
                return '.';
        }
    }

    public boolean withDecimals() {
        return this == DOT_COMMA || this == COMMA_DOT || this == NONE_DOT || this == NONE_COMMA;
    }

    public boolean withCentenals() {
        return this == DOT_COMMA || this == COMMA_DOT || this == DOT_NONE || this == COMMA_NONE;
    }

}
