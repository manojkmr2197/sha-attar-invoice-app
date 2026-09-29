package com.app.sha.attar.invoice.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.app.sha.attar.invoice.model.BillingInvoiceModel;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GSTReportPDFHelper {

    private static final int PAGE_WIDTH = 595; // A4 standard width
    private static final int PAGE_HEIGHT = 842; // A4 standard height
    private static final int MARGIN = 20;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final ZoneOffset IST_OFFSET = ZoneOffset.ofHoursMinutes(5, 30);

    public static File generateGSTPDFReport(Context context,
                                           List<BillingInvoiceModel> invoiceList,
                                           OffsetDateTime startDate,
                                           OffsetDateTime endDate,
                                           boolean gstOnly) throws Exception {

        List<BillingInvoiceModel> filteredInvoices = new ArrayList<>();
        if (invoiceList != null) {
            for (BillingInvoiceModel inv : invoiceList) {
                if (inv == null) continue;
                if (gstOnly) {
                    if (inv.getIsGSTApplicable() != null && inv.getIsGSTApplicable()) {
                        filteredInvoices.add(inv);
                    }
                } else {
                    filteredInvoices.add(inv);
                }
            }
        }

        // Compute overall totals safely
        double sumTaxable = 0.0;
        double sumCgst = 0.0;
        double sumSgst = 0.0;
        double sumIgst = 0.0;
        double sumGrandTotal = 0.0;

        for (BillingInvoiceModel inv : filteredInvoices) {
            if (inv == null) continue;
            boolean isGst = inv.getIsGSTApplicable() != null && inv.getIsGSTApplicable();
            double selling = inv.getSellingCost() != null ? inv.getSellingCost() : 0.0;
            double taxable = inv.getTaxableAmount() != null && inv.getTaxableAmount() > 0 ? inv.getTaxableAmount() : (isGst ? selling : selling);
            double cgst = inv.getCgstAmount() != null ? inv.getCgstAmount() : 0.0;
            double sgst = inv.getSgstAmount() != null ? inv.getSgstAmount() : 0.0;
            double igst = inv.getIgstAmount() != null ? inv.getIgstAmount() : 0.0;
            double roundOff = inv.getRoundOff() != null ? inv.getRoundOff() : 0.0;
            double courier = inv.getCourierAmount() != null ? inv.getCourierAmount() : 0.0;
            double grand = inv.getGrandTotal() != null ? inv.getGrandTotal() : (selling + cgst + sgst + igst + roundOff + courier);

            sumTaxable += taxable;
            sumCgst += cgst;
            sumSgst += sgst;
            sumIgst += igst;
            sumGrandTotal += grand;
        }

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int rowsPerPageFirst = 20; // First page has header and KPI cards
        int rowsPerPageSubsequent = 30;

        int totalInvoices = filteredInvoices.size();
        int remainingRows = Math.max(0, totalInvoices - rowsPerPageFirst);
        int totalPages = 1 + (int) Math.ceil((double) remainingRows / rowsPerPageSubsequent);
        if (totalInvoices == 0) totalPages = 1;

        int currentInvoiceIndex = 0;

        for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int y = MARGIN + 10;

            if (pageNum == 1) {
                // Header Banner
                paint.setColor(Color.parseColor("#1B2A4A"));
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paint.setTextSize(16f);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("SHA ATTAR", PAGE_WIDTH / 2f, y + 10, paint);
                y += 24;

                paint.setColor(Color.parseColor("#444444"));
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                paint.setTextSize(8.5f);
                canvas.drawText("A11, Gemini Parson Complex, Basement Floor, Kodambakkam High Rd, Chennai-600006", PAGE_WIDTH / 2f, y, paint);
                y += 12;
                canvas.drawText("Phone: +91 978 977 5134 | GSTIN: 33FIPPM7687P1ZZ", PAGE_WIDTH / 2f, y, paint);
                y += 16;

                // Title bar
                paint.setColor(Color.parseColor("#2A5298"));
                canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 22, paint);

                paint.setColor(Color.WHITE);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paint.setTextSize(10f);
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText("GST SALES & TAX AUDIT STATEMENT", MARGIN + 10, y + 15, paint);

                String periodStr = "Period: " +
                        (startDate != null ? startDate.format(DATE_FORMATTER) : "All") + " to " +
                        (endDate != null ? endDate.format(DATE_FORMATTER) : "All");
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(periodStr, PAGE_WIDTH - MARGIN - 10, y + 15, paint);
                y += 30;

                // KPI Summary Cards
                drawKpiBox(canvas, paint, MARGIN, y, 125, 38, "TOTAL BILLS", String.valueOf(totalInvoices), "#E8EEF5", "#1B2A4A");
                drawKpiBox(canvas, paint, MARGIN + 135, y, 130, 38, "TAXABLE VALUE", "₹" + String.format(Locale.ENGLISH, "%.2f", sumTaxable), "#E8F5E9", "#2E7D32");
                drawKpiBox(canvas, paint, MARGIN + 275, y, 130, 38, "TOTAL TAX (GST)", "₹" + String.format(Locale.ENGLISH, "%.2f", (sumCgst + sumSgst + sumIgst)), "#FFF3E0", "#E65100");
                drawKpiBox(canvas, paint, MARGIN + 415, y, 140, 38, "GRAND TOTAL", "₹" + String.format(Locale.ENGLISH, "%.2f", sumGrandTotal), "#EDE7F6", "#4A148C");
                y += 48;
            } else {
                // Secondary Page Header
                paint.setColor(Color.parseColor("#1B2A4A"));
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paint.setTextSize(10f);
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText("SHA ATTAR - GST SALES STATEMENT (Contd.)", MARGIN, y + 10, paint);

                paint.setColor(Color.GRAY);
                paint.setTextAlign(Paint.Align.RIGHT);
                paint.setTextSize(8.5f);
                canvas.drawText("Page " + pageNum + " of " + totalPages, PAGE_WIDTH - MARGIN, y + 10, paint);
                y += 20;
            }

            // Table Header Row
            paint.setColor(Color.parseColor("#37474F"));
            canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18, paint);

            paint.setColor(Color.WHITE);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setTextSize(7.5f);

            drawTableHeader(canvas, paint, y + 12);
            y += 18;

            int pageRowLimit = (pageNum == 1) ? rowsPerPageFirst : rowsPerPageSubsequent;
            int rowsDrawn = 0;

            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setTextSize(7f);

            while (currentInvoiceIndex < totalInvoices && rowsDrawn < pageRowLimit) {
                BillingInvoiceModel inv = filteredInvoices.get(currentInvoiceIndex);
                boolean isAlt = (rowsDrawn % 2 == 1);

                if (isAlt) {
                    paint.setColor(Color.parseColor("#F7F9FA"));
                    canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 16, paint);
                }

                paint.setColor(Color.parseColor("#212121"));
                drawTableRow(canvas, paint, inv, currentInvoiceIndex + 1, y + 11);

                paint.setColor(Color.parseColor("#E0E0E0"));
                canvas.drawLine(MARGIN, y + 16, PAGE_WIDTH - MARGIN, y + 16, paint);

                y += 16;
                rowsDrawn++;
                currentInvoiceIndex++;
            }

            // If last page, draw final total row
            if (pageNum == totalPages) {
                paint.setColor(Color.parseColor("#CFD8DC"));
                canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20, paint);

                paint.setColor(Color.BLACK);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paint.setTextSize(8f);
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText("TOTAL (" + totalInvoices + " Invoices)", MARGIN + 4, y + 13, paint);

                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText("₹" + String.format(Locale.ENGLISH, "%.2f", sumTaxable), MARGIN + 295, y + 13, paint);
                canvas.drawText("₹" + String.format(Locale.ENGLISH, "%.2f", sumCgst), MARGIN + 345, y + 13, paint);
                canvas.drawText("₹" + String.format(Locale.ENGLISH, "%.2f", sumSgst), MARGIN + 395, y + 13, paint);
                canvas.drawText("₹" + String.format(Locale.ENGLISH, "%.2f", sumIgst), MARGIN + 445, y + 13, paint);
                canvas.drawText("₹" + String.format(Locale.ENGLISH, "%.2f", sumGrandTotal), PAGE_WIDTH - MARGIN - 45, y + 13, paint);
            }

            // Footer
            paint.setColor(Color.GRAY);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setTextSize(7f);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Generated for GST Filing & Audit Purposes", MARGIN, PAGE_HEIGHT - 15, paint);

            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("Page " + pageNum + " of " + totalPages, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 15, paint);

            pdfDocument.finishPage(page);
        }

        // Save PDF
        String dateStr = (startDate != null && endDate != null)
                ? startDate.format(DATE_FORMATTER) + "_to_" + endDate.format(DATE_FORMATTER)
                : String.valueOf(System.currentTimeMillis());

        String fileName = "GST_Sales_Summary_" + dateStr + ".pdf";
        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File file = new File(exportDir, fileName);

        FileOutputStream fileOut = new FileOutputStream(file);
        pdfDocument.writeTo(fileOut);
        fileOut.close();
        pdfDocument.close();

        return file;
    }

    private static void drawTableHeader(Canvas canvas, Paint paint, int y) {
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("#", MARGIN + 4, y, paint);
        canvas.drawText("Date", MARGIN + 20, y, paint);
        canvas.drawText("Invoice ID", MARGIN + 70, y, paint);
        canvas.drawText("Customer Name", MARGIN + 125, y, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Taxable (₹)", MARGIN + 295, y, paint);
        canvas.drawText("CGST (₹)", MARGIN + 345, y, paint);
        canvas.drawText("SGST (₹)", MARGIN + 395, y, paint);
        canvas.drawText("IGST (₹)", MARGIN + 445, y, paint);
        canvas.drawText("Total (₹)", PAGE_WIDTH - MARGIN - 45, y, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Mode", PAGE_WIDTH - MARGIN - 20, y, paint);
    }

    private static void drawTableRow(Canvas canvas, Paint paint, BillingInvoiceModel inv, int sNo, int y) {
        if (inv == null) return;

        String date = "N/A";
        if (inv.getBillingDate() != null) {
            try {
                date = Instant.ofEpochSecond(inv.getBillingDate()).atOffset(IST_OFFSET).format(DATE_FORMATTER);
            } catch (Exception ignored) {}
        }

        String invId = inv.getBillingDate() != null ? String.valueOf(inv.getBillingDate()) : "N/A";
        String cust = StringUtils.isNotBlank(inv.getClientName()) ? inv.getClientName() :
                (StringUtils.isNotBlank(inv.getCustomerName()) ? inv.getCustomerName() : "Walk-in");
        if (cust.length() > 22) {
            cust = cust.substring(0, 20) + "..";
        }

        boolean isGst = inv.getIsGSTApplicable() != null && inv.getIsGSTApplicable();
        double selling = inv.getSellingCost() != null ? inv.getSellingCost() : 0.0;
        double taxable = inv.getTaxableAmount() != null && inv.getTaxableAmount() > 0 ? inv.getTaxableAmount() : (isGst ? selling : selling);
        double cgst = inv.getCgstAmount() != null ? inv.getCgstAmount() : 0.0;
        double sgst = inv.getSgstAmount() != null ? inv.getSgstAmount() : 0.0;
        double igst = inv.getIgstAmount() != null ? inv.getIgstAmount() : 0.0;
        double roundOff = inv.getRoundOff() != null ? inv.getRoundOff() : 0.0;
        double courier = inv.getCourierAmount() != null ? inv.getCourierAmount() : 0.0;
        double grand = inv.getGrandTotal() != null ? inv.getGrandTotal() : (selling + cgst + sgst + igst + roundOff + courier);
        String mode = StringUtils.isNotBlank(inv.getPaymentMode()) ? inv.getPaymentMode() : "CASH";

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(String.valueOf(sNo), MARGIN + 4, y, paint);
        canvas.drawText(date, MARGIN + 20, y, paint);
        canvas.drawText(invId, MARGIN + 70, y, paint);
        canvas.drawText(cust, MARGIN + 125, y, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.ENGLISH, "%.2f", taxable), MARGIN + 295, y, paint);
        canvas.drawText(String.format(Locale.ENGLISH, "%.2f", cgst), MARGIN + 345, y, paint);
        canvas.drawText(String.format(Locale.ENGLISH, "%.2f", sgst), MARGIN + 395, y, paint);
        canvas.drawText(String.format(Locale.ENGLISH, "%.2f", igst), MARGIN + 445, y, paint);
        canvas.drawText(String.format(Locale.ENGLISH, "%.2f", grand), PAGE_WIDTH - MARGIN - 45, y, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(mode, PAGE_WIDTH - MARGIN - 20, y, paint);
    }

    private static void drawKpiBox(Canvas canvas, Paint paint, float x, float y, float width, float height,
                                  String label, String value, String bgHex, String textHex) {
        RectF rect = new RectF(x, y, x + width, y + height);
        paint.setColor(Color.parseColor(bgHex));
        canvas.drawRoundRect(rect, 4, 4, paint);

        paint.setColor(Color.parseColor(textHex));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(7f);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, x + 6, y + 13, paint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(10f);
        canvas.drawText(value, x + 6, y + 29, paint);
    }
}
