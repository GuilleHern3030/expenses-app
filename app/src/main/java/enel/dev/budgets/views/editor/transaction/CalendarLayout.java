package enel.dev.budgets.views.editor.transaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.fragment.app.Fragment;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.Date;

public class CalendarLayout extends Fragment {

    private Date dateSelected;

    public static CalendarLayout newInstance(final Date date) {
        Bundle args = new Bundle();
        args.putString("date", date.toString());
        CalendarLayout fragment = new CalendarLayout();
        fragment.setArguments(args);
        return fragment;
    }

    public CalendarLayout() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.dateSelected = new Date(getArguments().getString("date"));
        } catch (Exception ignored) {
            listener.onCancelOperation();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transaction_calendar, container, false);

        createCalendar(view);

        view.findViewById(R.id.bAccept).setOnClickListener(v -> listener.onDateChanged(this.dateSelected));
        view.findViewById(R.id.bCancel).setOnClickListener(v -> listener.onCancelOperation());

        return view;
    }

    private void createCalendar(final View view) {
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setDate(dateSelected.toLong());
        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            try {
                this.dateSelected = new Date(year, (month + 1), dayOfMonth);
            } catch(Exception ignored) { }
        });
    }

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    private OnTransactionDateListener listener;
    public interface OnTransactionDateListener {
        void onDateChanged(final Date newDate);
        void onCancelOperation();
    }

    public void setOnTransactionDateListener(OnTransactionDateListener listener) {
        this.listener = listener;
    }
    //</editor-fold>
}