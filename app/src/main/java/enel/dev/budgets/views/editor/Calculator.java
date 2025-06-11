package enel.dev.budgets.views.editor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.objects.NumberFormat;
import enel.dev.budgets.utils.SnackBar;

public class Calculator extends Fragment {

    private TextView input;
    private TextView oldInput;
    private LinearLayout calculatorDigits;
    private final String coinSymbol;
    private final NumberFormat decimalFormat;
    private final Activity context;
    private final FrameLayout layout;

    public Calculator(final Activity context, final FrameLayout layout, final NumberFormat decimalFormat, final String coinSymbol) {
        super();
        this.coinSymbol = coinSymbol;
        this.decimalFormat = decimalFormat;
        this.context = context;
        this.layout = layout;
    }

    public Calculator(final Activity context, final FrameLayout layout, final NumberFormat decimalFormat) {
        this(context, layout, decimalFormat, Preferences.DEFAULT_COIN_SYMBOL);
    }

    public Calculator(final Activity context, final FrameLayout layout) {
        this(context, layout, Preferences.DEFAULT_DECIMAL_FORMAT, Preferences.DEFAULT_COIN_SYMBOL);
    }

    public void showCalculator() {
        showCalculator(0);
    }

    public void showCalculator(final double amount) {
        showCalculator(
                amount,
                ContextCompat.getColor(context, R.color.income_text),
                R.drawable.border_input_income
        );
    }

    @SuppressLint("DefaultLocale")
    public void showCalculator(final double amount, final int textColor, @DrawableRes final int inputColor) {
        layout.removeAllViews();

        View view = LayoutInflater.from(context).inflate(R.layout.fragment_calculator, layout, false);
        calculatorDigits = view.findViewById(R.id.calculator_digits);
        calculatorDigits.setVisibility(View.VISIBLE);
        input = view.findViewById(R.id.amount);
        oldInput = view.findViewById(R.id.amount_old);
        createCalculator(view);

        input.setTextColor(textColor);
        input.setText(format(amount));

        TextView coinSymbolTextView = view.findViewById(R.id.amount_coin);
        coinSymbolTextView.setTextColor(textColor);
        coinSymbolTextView.setText(coinSymbol);

        FrameLayout inputContainer = view.findViewById(R.id.input_box);
        inputContainer.setBackgroundResource(inputColor);

        ImageView removeIcon = view.findViewById(R.id.remove_image);
        removeIcon.setColorFilter(textColor);

        if (!decimalFormat.withDecimals())
            view.findViewById(R.id.calculator_digits_decimal).setVisibility(View.GONE);

        layout.addView(view);
    }

    public double getInput() {
        addResolution(oldInput);
        return Double.parseDouble(input.getText().toString().replace(",", "."));
    }

    public void setVisibility(int visibility) {
        layout.setVisibility(visibility);
    }

    public void hideDigits() {
        calculatorDigits.setVisibility(View.GONE);
    }

    public void showDigits() {
        calculatorDigits.setVisibility(View.VISIBLE);
    }

    //<editor-fold defaultstate="collapsed" desc=" Calculator ">
    private void createCalculator(final View view) {
        view.findViewById(R.id.calculator_digits).setVisibility(View.VISIBLE);
        view.findViewById(R.id.calculator_remove).setOnLongClickListener(v -> clearDigits(oldInput));
        view.findViewById(R.id.calculator_remove).setOnClickListener(v -> removeDigit(oldInput));
        view.findViewById(R.id.calculator_digits_equals).setOnClickListener(v -> addResolution(oldInput));
        view.findViewById(R.id.calculator_digits_divide).setOnClickListener(v -> addOperator("/", oldInput));
        view.findViewById(R.id.calculator_digits_product).setOnClickListener(v -> addOperator("x", oldInput));
        view.findViewById(R.id.calculator_digits_plus).setOnClickListener(v -> addOperator("+", oldInput));
        view.findViewById(R.id.calculator_digits_substract).setOnClickListener(v -> addOperator("-", oldInput));
        view.findViewById(R.id.calculator_digits_decimal).setOnClickListener(v -> addDecimal());
        view.findViewById(R.id.calculator_digits_1).setOnClickListener(v -> addDigit("1"));
        view.findViewById(R.id.calculator_digits_2).setOnClickListener(v -> addDigit("2"));
        view.findViewById(R.id.calculator_digits_3).setOnClickListener(v -> addDigit("3"));
        view.findViewById(R.id.calculator_digits_4).setOnClickListener(v -> addDigit("4"));
        view.findViewById(R.id.calculator_digits_5).setOnClickListener(v -> addDigit("5"));
        view.findViewById(R.id.calculator_digits_6).setOnClickListener(v -> addDigit("6"));
        view.findViewById(R.id.calculator_digits_7).setOnClickListener(v -> addDigit("7"));
        view.findViewById(R.id.calculator_digits_8).setOnClickListener(v -> addDigit("8"));
        view.findViewById(R.id.calculator_digits_9).setOnClickListener(v -> addDigit("9"));
        view.findViewById(R.id.calculator_digits_0).setOnClickListener(v -> {
            if (!input.getText().toString().equals("0"))
                addDigit("0");
        });
    }

    @SuppressLint("SetTextI18n")
    private void addDigit(final String digit) {
        try {
            final String prev = input.getText().toString();
            final int len = prev.length();
            final int decimalIndex = prev.contains(".") ? prev.indexOf(".") : prev.indexOf(",");
            if (prev.length() < 15 && (len - decimalIndex != 3 || decimalIndex == -1)) {
                if (prev.equals("0") && (!digit.equals(".") && !digit.equals(",")))
                    input.setText(digit);
                else input.setText(prev + digit);
            }
        } catch (Exception e) { Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show(); }
    }

    private void removeDigit(TextView oldInput) {
        try {
            if (input.getText().toString().equals("0") && oldInput.getVisibility() == View.VISIBLE) {
                input.setText(oldInput.getText().toString().substring(0, oldInput.getText().toString().length()-1));
                oldInput.setText("");
                oldInput.setVisibility(View.GONE);
            }
            else if (input.getText().toString().length() == 1)
                input.setText("0");
            else //if (!input.getText().toString().equals("0"))
                input.setText(input.getText().toString().substring(0, input.getText().toString().length()-1));
        } catch (Exception e) { Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show(); }
    }

    private void addDecimal() {
        if (decimalFormat.withDecimals()) try {
            if (!input.getText().toString().contains(".") && !input.getText().toString().contains(",")) try {
                addDigit(String.valueOf(Preferences.decimalFormat(requireActivity())));
            } catch (Exception ignored) {
                addDigit(".");
            }
        } catch (Exception e) { Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show(); }
    }

    @SuppressLint("SetTextI18n")
    private void addOperator(final String operator, TextView oldInput) {
        try {
            if (input.getText().toString().equals("0")) removeDigit(oldInput);
            else if (oldInput.getVisibility() == View.VISIBLE) addResolution(oldInput);
            oldInput.setVisibility(View.VISIBLE);
            oldInput.setText(input.getText().toString() + operator);
            input.setText("0");
        } catch (Exception e) { Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show(); }
    }

    private void addResolution(TextView oldInput) {
        if (oldInput != null && oldInput.getVisibility() == View.VISIBLE) try {
            final String operator = oldInput.getText().toString().substring(oldInput.getText().toString().length()-1);
            final double operando1 = Double.parseDouble(oldInput.getText().toString().replace(",", ".").substring(0, oldInput.getText().toString().length()-1));
            final double operando2 = Double.parseDouble(input.getText().toString().replace(",", "."));
            String result = "0";
            switch (operator) {
                case "+": {
                    result = String.format(Locale.US, "%.2f", (operando1 + operando2));
                    break;
                }
                case "-": {
                    result = String.format(Locale.US, "%.2f", (operando1 - operando2));
                    break;
                }
                case "x": {
                    result = String.format(Locale.US, "%.2f", (operando1 * operando2));
                    break;
                }
                case "/": {
                    result = operando2 != 0 ?
                            String.format(Locale.US, "%.2f", (operando1 / operando2)) :
                            String.format(Locale.US, "%.2f", operando1);
                    break;
                }
            }
            if (Double.parseDouble(result) < 0) result = "0"; // No negative numbers
            else if (result.endsWith("00")) result = result.substring(0, result.length()-3);
            else if (result.endsWith("0")) result = result.substring(0, result.length()-1);
            input.setText(result);
        } catch(Exception e) {
            Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_LONG).show();
            input.setText("0");
        } finally {
            oldInput.setText("");
            oldInput.setVisibility(View.GONE);
        }
    }

    private boolean clearDigits(TextView oldInput) {
        input.setText("0");
        oldInput.setText("");
        oldInput.setVisibility(View.GONE);
        return false;
    }
    //</editor-fold>

    private String format(final double amount) {
        String _amount = String.format(Locale.US, "%.2f", amount);
        if (_amount.endsWith(".00")) _amount = _amount.substring(0, _amount.length() - 3);
        else if (_amount.endsWith("0")) _amount = _amount.substring(0, _amount.length() - 1);
        return _amount;
    }
}