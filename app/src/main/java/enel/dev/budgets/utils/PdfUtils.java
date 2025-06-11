package enel.dev.budgets.utils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/*

    Para crear un PDF es necesario usar este codigo:

    private void createPdf() {
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_TITLE, "example.pdf");
            startActivityForResult(intent, PdfUtils.REQUEST_CODE);
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PdfUtils.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) try {
                Uri uri = data.getData();
                if (uri != null)
                    PdfUtils.createPdfFromScalableLayout(requireActivity(), pdfLayout, uri);
            } catch (Exception e) {
                showError(e.toString());
            }
        }
    }


 */


@SuppressWarnings("unused")
public class PdfUtils {

    public static int REQUEST_CODE = 1231;

    // A4 page dimensions in points (1 point = 1/72 inch)
    private final static int A4_WIDTH = 595;
    private final static int A4_HEIGHT = 842;

    /**
     * Convierte el layout en un outputStream compatible con PDF (será necesario que el layout tenga un width de 595pt)
     * @param context context
     * @param layout view que se quiere convertir en pdf
     * @param marginVertical 72 = 1 point
     */
    public static void createPdfFromLayout(final Context context, final View layout, final Uri uri, final int marginVertical) throws FileNotFoundException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "w");
        assert pfd != null;
        FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());

        // A4 page dimensions in points (1 point = 1/72 inch)
        final int pageWidth = A4_WIDTH; // A4 width in points
        final int pageHeight = A4_HEIGHT; // A4 height in points

        // Margins in points (vertical margins only)
        final int marginTop; // 1 inch
        marginTop = marginVertical;
        final int marginBottom;
        marginBottom= marginVertical; // 1 inch

        // Height available for content after applying vertical margins
        final int contentHeight = pageHeight - marginTop - marginBottom;

        // Calculate the scale factor based on the width of the layout
        final int layoutWidth = layout.getWidth();
        final float scale = pageWidth / (float) layoutWidth;

        // Create a new PdfDocument
        PdfDocument pdfDocument = new PrintedPdfDocument(context, new android.print.PrintAttributes.Builder()
                .setMediaSize(android.print.PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new android.print.PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                .setMinMargins(android.print.PrintAttributes.Margins.NO_MARGINS)
                .build());

        // Start creating the PDF
        final int totalHeight = layout.getHeight();
        int currentPageNumber = 0;
        int currentHeight = 0;

        while (currentHeight < totalHeight) {
            currentPageNumber++;

            // Create a new page
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            // Translate the canvas to start drawing the content after the top margin
            canvas.translate(0, marginTop);

            // Scale the canvas to fit the width
            canvas.scale(scale, scale);

            // Translate the canvas to draw the correct portion of the layout
            canvas.translate(0, -currentHeight);

            // Draw the view on the canvas
            layout.draw(canvas);

            // Reset the canvas translation to draw the white stripes over the content
            canvas.translate(0, currentHeight);
            canvas.scale(1/scale, 1/scale);
            canvas.translate(0, -marginTop);

            // Paint for the white stripes
            Paint whitePaint = new Paint();
            whitePaint.setColor(Color.WHITE);

            // Draw a white rectangle as the top margin on every page
            canvas.drawRect(0, 0, pageWidth, marginTop, whitePaint); // Top white stripe

            // Draw a white rectangle as the bottom margin on every page
            canvas.drawRect(0, pageHeight - marginBottom, pageWidth, pageHeight, whitePaint); // Bottom white stripe

            // Finish the page
            pdfDocument.finishPage(page);

            // Move to the next section of the layout
            currentHeight += contentHeight / scale; // Adjust for scale and content area
        }

        // Write the document content to the file
        try {
            pdfDocument.writeTo(fos);
            fos.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pdfDocument.close();
        }
    }

    public static File createPdfFromLayout(final Context context, final View layout, final String pdfName, final int marginVertical) {
        // Define el archivo temporal donde se guardará el PDF
        File pdfFile = new File(context.getCacheDir(), pdfName+".pdf");

        try {
            FileOutputStream fos = new FileOutputStream(pdfFile);

            // A4 page dimensions in points (1 point = 1/72 inch)
            final int pageWidth = A4_WIDTH; // A4 width in points
            final int pageHeight = A4_HEIGHT; // A4 height in points

            // Margins in points (vertical margins only)
            final int marginTop = marginVertical;
            final int marginBottom = marginVertical;

            // Height available for content after applying vertical margins
            final int contentHeight = pageHeight - marginTop - marginBottom;

            // Calculate the scale factor based on the width of the layout
            final int layoutWidth = layout.getWidth();
            final float scale = pageWidth / (float) layoutWidth;

            // Create a new PdfDocument
            PdfDocument pdfDocument = new PrintedPdfDocument(context, new android.print.PrintAttributes.Builder()
                    .setMediaSize(android.print.PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(new android.print.PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                    .setMinMargins(android.print.PrintAttributes.Margins.NO_MARGINS)
                    .build());

            // Start creating the PDF
            final int totalHeight = layout.getHeight();
            int currentPageNumber = 0;
            int currentHeight = 0;

            while (currentHeight < totalHeight) {
                currentPageNumber++;

                // Create a new page
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                // Translate the canvas to start drawing the content after the top margin
                canvas.translate(0, marginTop);

                // Scale the canvas to fit the width
                canvas.scale(scale, scale);

                // Translate the canvas to draw the correct portion of the layout
                canvas.translate(0, -currentHeight);

                // Draw the view on the canvas
                layout.draw(canvas);

                // Reset the canvas translation to draw the white stripes over the content
                canvas.translate(0, currentHeight);
                canvas.scale(1/scale, 1/scale);
                canvas.translate(0, -marginTop);

                // Paint for the white stripes
                Paint whitePaint = new Paint();
                whitePaint.setColor(Color.WHITE);

                // Draw a white rectangle as the top margin on every page
                canvas.drawRect(0, 0, pageWidth, marginTop, whitePaint); // Top white stripe

                // Draw a white rectangle as the bottom margin on every page
                canvas.drawRect(0, pageHeight - marginBottom, pageWidth, pageHeight, whitePaint); // Bottom white stripe

                // Finish the page
                pdfDocument.finishPage(page);

                // Move to the next section of the layout
                currentHeight += contentHeight / scale; // Adjust for scale and content area
            }

            // Write the document content to the file
            pdfDocument.writeTo(fos);
            fos.close();
            pdfDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pdfFile;
    }

    public static void sharePdfFile(Context context, File pdfFile) {
        // Convertir el archivo en un Uri usando FileProvider
        Log.d("FileProvider", "sharePdfFile: " + pdfFile.toURI().toString());
        Uri pdfUri = FileProvider.getUriForFile(context, "enel.dev.budgets.fileprovider", pdfFile);



        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Verificar que hay aplicaciones para manejar el intent
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(shareIntent, "Compartir PDF usando"));
        }
    }

    /**
     * Convierte el layout en un outputStream compatible con PDF (será necesario que el layout tenga un width de 595pt)
     * @param context context
     * @param layout view que se quiere convertir en pdf
     */
    public static void createPdfFromLayout(final Context context, final View layout, final Uri uri) throws FileNotFoundException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "w");
        assert pfd != null;
        FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());

        // A4 page dimensions in points (1 point = 1/72 inch)
        int pageWidth = A4_WIDTH; // A4 width in points
        int pageHeight = A4_HEIGHT; // A4 height in points

        // Calculate the scale factor
        int layoutWidth = layout.getWidth();
        float scale = pageWidth / (float) layoutWidth;

        // Create a new PdfDocument
        PdfDocument pdfDocument = new PrintedPdfDocument(context, new android.print.PrintAttributes.Builder()
                .setMediaSize(android.print.PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new android.print.PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                .setMinMargins(android.print.PrintAttributes.Margins.NO_MARGINS)
                .build());

        // Start creating the PDF
        int totalHeight = layout.getHeight();
        int currentPageNumber = 0;
        int currentHeight = 0;

        // Paint for the text
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        while (currentHeight < totalHeight) {
            currentPageNumber++;

            // Create a new page
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            // Scale the canvas
            canvas.scale(scale, scale);

            // Translate the canvas to the current height
            canvas.translate(0, -currentHeight);

            // Draw the view on the canvas
            layout.draw(canvas);

            // Finish the page
            pdfDocument.finishPage(page);

            // Move to the next section of the layout
            currentHeight += pageHeight / scale; // Adjust for scale
        }

        // Write the document content to the file
        try {
            pdfDocument.writeTo(fos);
            fos.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pdfDocument.close();
        }
    }

}
