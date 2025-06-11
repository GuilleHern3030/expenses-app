package enel.dev.budgets.views.pdf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.io.File;

import enel.dev.budgets.R;
import enel.dev.budgets.data.preferences.Preferences;
import enel.dev.budgets.objects.Date;
import enel.dev.budgets.objects.money.Money;
import enel.dev.budgets.objects.transaction.Transaction;
import enel.dev.budgets.objects.transaction.Transactions;
import enel.dev.budgets.objects.transaction.TransactionsArray;
import enel.dev.budgets.utils.PdfUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PdfFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PdfFragment extends Fragment {

    public PdfFragment() {
        // Required empty public constructor
    }

    private boolean incomes;
    private boolean expenses;

    private Date date1;
    private Date date2;

    private final Transactions transactions = new Transactions();

    private View pdf;

    public static PdfFragment newInstance(final Transactions transactionList, boolean incomes, boolean expenses, final Date date1, final Date date2) {
        PdfFragment fragment = new PdfFragment();
        Transactions transactions;
        if (!incomes) transactions = transactionList.expenses();
        else if (!expenses) transactions = transactionList.incomes();
        else transactions = transactionList;
        Bundle args = new Bundle();
        args.putBoolean("incomes", incomes);
        args.putBoolean("expenses", expenses);
        args.putString("date1", date1.toString());
        args.putString("date2", date2.toString());
        for (int i = 0; i < transactions.size(); i++)
            args.putString("transaction"+i, transactions.get(i).serialize());
        ;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) try {

            incomes = getArguments().getBoolean("incomes");
            expenses = getArguments().getBoolean("expenses");

            int i = 0;
            while(!getArguments().getString("transaction"+i, "").isEmpty()) {
                final String serialized = getArguments().getString("transaction"+i);
                final Transaction transaction = Transaction.newInstance(serialized);
                if (transaction != null)
                    transactions.add(transaction);
                i++;
            }

            date1 = new Date(getArguments().getString("date1"));
            date2 = new Date(getArguments().getString("date2"));

        } catch (Exception e) {
            Log.e("PDF_FRAGMENT_ERROR", "onCreate: ", e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        closeFragment();
                    }
                });

        return inflater.inflate(R.layout.fragment_pdf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pdf = view.findViewById(R.id.pdf_container);

        if (transactions.size() > 0) {

            final Transactions expensesList = transactions.expenses();
            final Transactions incomesList = transactions.incomes();

            final double expensesTotal = expensesList.total(Preferences.defaultCoin(requireActivity())).getAmount();
            final double incomesTotal = incomesList.total(Preferences.defaultCoin(requireActivity())).getAmount();

            view.findViewById(R.id.cancel_pdf_button).setOnClickListener(v -> closeFragment());
            view.findViewById(R.id.download_pdf_button).setOnClickListener(v -> sharePdf());

            ((TextView)view.findViewById(R.id.pdf_title)).setText(getTitle(expenses, incomes));
            ((TextView)view.findViewById(R.id.pdf_subtitle)).setText(getDateText(date1, date2));

            if (incomes) {
                final SummaryListPdf summaryList = new SummaryListPdf(requireActivity(), view.findViewById(R.id.summary_list_incomes_pdf), Preferences.decimalFormat(requireActivity()));
                summaryList.showContent(incomesList.getCategoriesSorted(), incomesTotal);
                view.findViewById(R.id.summary_list_incomes_pdf_container).setVisibility(View.VISIBLE);
            }

            if (expenses) {
                SummaryListPdf summaryList = new SummaryListPdf(requireActivity(), view.findViewById(R.id.summary_list_expenses_pdf), Preferences.decimalFormat(requireActivity()));
                summaryList.showContent(expensesList.getCategoriesSorted(), expensesTotal);
                view.findViewById(R.id.summary_list_expenses_pdf_container).setVisibility(View.VISIBLE);
            }

            if (incomes && expenses) {
                view.findViewById(R.id.summary_list_expenses_pdf_title).setVisibility(View.VISIBLE);
                view.findViewById(R.id.summary_list_incomes_pdf_title).setVisibility(View.VISIBLE);
                final String amount = Money.toString(
                        Preferences.defaultCoin(requireActivity()).getSymbol(),
                        (incomesTotal - expensesTotal),
                        Preferences.decimalFormat(requireActivity())
                );
            }

            TransactionsByDayListPdf transactionsList = new TransactionsByDayListPdf(requireActivity(), view.findViewById(R.id.transactions_list_pdf), Preferences.decimalFormat(requireActivity()));
            transactionsList.showContent(new TransactionsArray(transactions));

            ((TextView)view.findViewById(R.id.transactions_list_total_pdf)).setText(transactions.balance().total(Preferences.defaultCoin(requireActivity())).toString(Preferences.decimalFormat(requireActivity())));

            scalePdf(pdf, view.findViewById(R.id.screen_background));

        } else closeFragment();
    }

    public void scalePdf(final View pdf, final View screen) {

        if (pdf != null && screen != null) try {

            ViewTreeObserver observer = pdf.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    pdf.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // Obtener dimensiones de la pantalla
                    final int screenWidth = screen.getWidth();
                    final int screenHeight = screen.getHeight();

                    // Calcular la escala para ajustar el tamaño A4 a la pantalla
                    final float scale = (float) screenWidth / pdf.getWidth();

                    // Aplicar la escala al FrameLayout
                    pdf.setScaleX(scale);
                    pdf.setScaleY(scale);

                    // Ajustar la posición del FrameLayout para alinearlo en la parte superior izquierda
                    final float pivotX = 0;
                    final float pivotY = 0;
                    pdf.setPivotX(pivotX);
                    pdf.setPivotY(pivotY);

                }
            });
        } catch (IllegalArgumentException ignored) { }
    }

    //<editor-fold defaultstate="collapsed" desc=" Listener ">
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setOnFragmentPdfListener(OnFragmentInteractionListener listener) {
        this.mListener = listener;
    }

    private OnFragmentInteractionListener mListener;
    public interface OnFragmentInteractionListener {
        void onCloseFragment();
    }

    private void closeFragment() {
        mListener.onCloseFragment();
    }
    //</editor-fold>

    private String getTitle(final boolean expenses, final boolean incomes) {
        return expenses && incomes ? requireActivity().getString(R.string.balance) :
                incomes ? requireActivity().getString(R.string.incomes) :
                        requireActivity().getString(R.string.expenses);
    }

    private String getDateText(final Date date1, final Date date2) {
        return (date1.partialEncode() != date2.partialEncode()) ?
                date1.formatedDateExtended(requireActivity()) + " - " + date2.formatedDateExtended(requireActivity()) :
                (date1.encode() != date2.encode()) ?
                    date1.formatedDateExtended(requireActivity()) :
                    date1.getMonthNameAndYear(requireActivity());

    }


    //<editor-fold defaultstate="collapsed" desc=" PDF ">
    private void sharePdf() {
        String fileName = getTitle(expenses, incomes) + " " + getDateText(date1, date2);
        File pdfFile = PdfUtils.createPdfFromLayout(requireActivity(), pdf, fileName, 72);
        PdfUtils.sharePdfFile(requireActivity(), pdfFile);
    }/*

    private void createPdf() {
        try {
            String fileName = getTitle(expenses, incomes) + " " + getDateText(date1, date2);
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_TITLE, fileName + ".pdf");
            startActivityForResult(intent, PdfUtils.REQUEST_CODE);
        } catch (Exception ignored) {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PdfUtils.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) try {
                Uri uri = data.getData();
                if (uri != null)
                    PdfUtils.createPdfFromLayout(requireActivity(), pdf, uri, 72);
            } catch (Exception ignored) {
            }
        }
    }*/
    //</editor-fold>
}