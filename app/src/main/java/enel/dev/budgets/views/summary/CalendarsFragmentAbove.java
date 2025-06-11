package enel.dev.budgets.views.summary;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import enel.dev.budgets.R;
import enel.dev.budgets.objects.Date;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class CalendarsFragmentAbove extends Fragment {

    private Date initDate = null;

    public CalendarsFragmentAbove() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summary_calendars_above, container, false);

        view.findViewById(R.id.bExit).setOnClickListener(v -> closeFragment());

        createCalendar(view);

        return view;
    }

    private void createCalendar(View view) {

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {

            try {
                if (this.initDate == null) {
                    this.initDate = new Date(year, (month + 1), dayOfMonth, 0);
                    TextView tvTitle = view.findViewById(R.id.calendar_title);
                    tvTitle.setText(requireActivity().getString(R.string.transactions_calendar_ends));
                    tvTitle.setBackgroundResource(R.color.action_bar_background);
                    Toast.makeText(requireActivity(), requireActivity().getString(R.string.transactions_calendar_require_new_date), Toast.LENGTH_SHORT).show();
                } else {
                    final Date endDate = new Date(year, (month + 1), dayOfMonth, 23);
                    if (initDate.isBefore(endDate))
                        changeDates(this.initDate, endDate);
                    else changeDates(new Date(endDate.getYear(), endDate.getMonth(), endDate.getDay(), 0),
                            new Date(this.initDate.getYear(), this.initDate.getMonth(), this.initDate.getDay(), 23));
                }
            } catch(Exception e) {
                closeFragment();
            }

        });
    }

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        this.mListener = listener;
    }

    private OnFragmentInteractionListener mListener;
    public interface OnFragmentInteractionListener {
        void onCloseFragment();
        void onDateChanged(final Date date1, final Date date2);
    }

    private void closeFragment() {
        mListener.onCloseFragment();
    }
    private void changeDates(final Date date1, final Date date2) {
        mListener.onDateChanged(date1, date2);
    }
    //</editor-fold>
}