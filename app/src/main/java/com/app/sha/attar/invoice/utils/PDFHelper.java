package com.app.sha.attar.invoice.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.GroupedItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PDFHelper {

    private Context context;

    public PDFHelper(Context context) {
        this.context = context;
    }

    public String createPdfAndShare(BillingInvoiceModel billData) {
        int pageWidth = 576; // For 3-inch printer @203 DPI
        int marginTop = 20;
        int y = marginTop;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(22f);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        // Estimate height
        int lineHeight = 30;
        int logoHeight = 150;
        int estimatedLines = 0;

        estimatedLines += 6; // Header lines
        estimatedLines += 4; // Address and footer
        estimatedLines += billData.getBillingItemModelList().size(); // Items
        if (billData.getDiscount() > 0) estimatedLines += 2;
        estimatedLines += 10; // separators, thank you, etc.

        int pageHeight = marginTop + logoHeight + (estimatedLines * lineHeight) + 100;

        // Create PDF
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Draw Logo
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_print_logo);
        Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, 150, 150, false);
        canvas.drawBitmap(scaledLogo, (pageWidth - scaledLogo.getWidth()) / 2f, y, paint);
        y += logoHeight + 20;

        // Header
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("SHA'S ATTAR & PERFUMES", pageWidth / 2f, y, paint);
        y += 30;
        paint.setTextSize(18f);
        canvas.drawText("(Make your own Perfume)", pageWidth / 2f, y, paint);
        y += 40;

        // Date
        paint.setTextSize(16f);
        paint.setTextAlign(Paint.Align.RIGHT);
        String dateStr = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());
        canvas.drawText("Date: " + dateStr.toUpperCase(), pageWidth - 20, y, paint);
        y += 40;

        // Order details
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText("ORDER No: " + billData.getBillingDate(), 10, y, paint);
        y += 30;
        canvas.drawText("Bill by: " + billData.getCustomerName().toUpperCase(), 10, y, paint);
        y += 30;

        // Separator

        int starWidth = (int) paint.measureText("*");
        int starCount = pageWidth / starWidth;
        String starLine = new String(new char[starCount]).replace('\0', '*');
        paint.setTextAlign(Paint.Align.LEFT);

        int dashWidth = (int) paint.measureText("-");
        int dashCount = pageWidth / dashWidth;

        // Build the line
        String dashLine = new String(new char[dashCount]).replace('\0', '-');
        canvas.drawText(dashLine, 0, y, paint);  // start at X=0
        //canvas.drawText(dashLine, pageWidth / 2f, y, paint);
        y += 20;

        // Table header
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("PRODUCT", 10, y, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("QTY", pageWidth / 2f, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("PRICE", pageWidth - 20, y, paint);
        y += 20;

        // Another separator
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(dashLine, 0, y, paint);
        y += 20;

        float productX = 10;
        float qtyX = pageWidth / 2f;
        float priceX = pageWidth - 20;
        float productMaxWidth = pageWidth * 0.55f;
        int prdlineHeight = 28;

        paint.setTextAlign(Paint.Align.LEFT);

        List<GroupedItem> groupedItems =
                groupBillingItems(billData.getBillingItemModelList());

        for (GroupedItem item : groupedItems) {

            String displayName = item.name;

            if (item.count > 1) {
                displayName += " [" + item.count +
                        " x Rs." + String.format("%.2f", item.unitPrice) + "]";
            }

            String qty = item.units != null ? item.units + " ML" : "";
            String price = "Rs." + String.format("%.2f", item.totalSellingPrice);

            // Draw multiline product name
            int linesUsed = drawMultilineText(
                    canvas,
                    paint,
                    displayName,
                    productX,
                    y,
                    productMaxWidth,
                    prdlineHeight
            );

            // QTY & PRICE only on first line
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(qty, qtyX, y, paint);

            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(price, priceX, y, paint);

            y += (linesUsed * prdlineHeight);

            paint.setTextAlign(Paint.Align.LEFT);
        }

        y += 10;

        // Separator
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(dashLine, 0, y, paint);
        y += 30;

        // =====================
// PROFESSIONAL TOTALS
// =====================
        paint.setTextSize(18f);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));

        double sellingCost = billData.getSellingCost();

// Subtotal
        drawRow(canvas, paint, "Subtotal",
                "Rs." + String.format("%.2f", billData.getTotalCost()),
                y, pageWidth);
        y += 25;

// Discount
        if (billData.getDiscount() > 0) {
            drawRow(canvas, paint,
                    "Discount (" + billData.getDiscount() + "%)",
                    "-Rs." + String.format("%.2f",
                            billData.getTotalCost() - sellingCost),
                    y, pageWidth);
            y += 25;
        }

// Courier
        if (billData.getIsCourier() != null && billData.getIsCourier()) {
            drawRow(canvas, paint,
                    "Courier Charges",
                    "Rs." + String.format("%.2f", billData.getCourierAmount()),
                    y, pageWidth);
            y += 25;

            sellingCost += billData.getCourierAmount();
        }

// Separator
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(dashLine, 0, y, paint);
        y += 25;

// -----------------
// CARD CHARGES
// -----------------
        if ("CARD".equalsIgnoreCase(billData.getPaymentMode())) {

            double cardCharge = round(sellingCost * 0.03);
            double gst = round(cardCharge * 0.18);
            double finalPayable = round(sellingCost + cardCharge + gst);

            drawRow(canvas, paint,
                    "Card Charges (3%)",
                    "Rs." + String.format("%.2f", cardCharge),
                    y, pageWidth);
            y += 25;

            drawRow(canvas, paint,
                    "GST on Card Charges (18%)",
                    "Rs." + String.format("%.2f", gst),
                    y, pageWidth);
            y += 25;

            // Separator
            canvas.drawText(dashLine, 0, y, paint);
            y += 30;

            // FINAL PAYABLE (HIGHLIGHT)
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            paint.setTextSize(22f);

            drawRow(canvas, paint,
                    "FINAL PAYABLE",
                    "Rs." + String.format("%.2f", finalPayable),
                    y, pageWidth);
            y += 40;

        } else {
            // CASH / UPI
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            paint.setTextSize(22f);

            drawRow(canvas, paint,
                    "TOTAL",
                    "Rs." + String.format("%.2f", sellingCost),
                    y, pageWidth);
            y += 40;
        }


        // Footer
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(16f);
        canvas.drawText("Payment: " + billData.getPaymentMode(), 10, y, paint);
        y += 30;

        // Address
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(starLine, 0, y, paint);
        y += 30;
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("A11, Gemini Parson Complex, Basement Floor,", pageWidth / 2f, y, paint);
        y += 25;
        canvas.drawText("Kodambakkam High Road, Nungambakkam.", pageWidth / 2f, y, paint);
        y += 25;
        canvas.drawText("Chennai-600006", pageWidth / 2f, y, paint);
        y += 25;
        canvas.drawText("Phone No : +91 978 977 5134", pageWidth / 2f, y, paint);
        y += 30;
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(starLine, 0, y, paint);
        y += 30;
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Thank you for shopping with us!", pageWidth / 2f, y, paint);
        y += 25;
        canvas.drawText("**ALL SALES ARE FINAL**", pageWidth / 2f, y, paint);
        y += 25;
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(starLine, 0, y, paint);

        pdfDocument.finishPage(page);

        return SavePDFFileAndShare(billData, pdfDocument);
    }


    private String SavePDFFileAndShare(BillingInvoiceModel billData, PdfDocument pdfDocument) {
        // Save PDF
        String fileName = "invoice-" + billData.getBillingDate() + "-" + OffsetDateTime.now().toEpochSecond() + ".pdf";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();
        return fileName;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void drawRow(Canvas canvas, Paint paint,
                         String label, String value,
                         int y, int pageWidth) {

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, 10, y, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(value, pageWidth - 10, y, paint);
    }


    private int drawMultilineText(Canvas canvas, Paint paint,
                                  String text, float x, int y,
                                  float maxWidth, int lineHeight) {

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = line + word + " ";
            if (paint.measureText(testLine) <= maxWidth) {
                line.append(word).append(" ");
            } else {
                lines.add(line.toString());
                line = new StringBuilder(word + " ");
            }
        }
        lines.add(line.toString());

        for (String l : lines) {
            canvas.drawText(l.trim(), x, y, paint);
            y += lineHeight;
        }

        return lines.size(); // number of lines drawn
    }

    private List<GroupedItem> groupBillingItems(List<BillingItemModel> items) {

        Map<String, GroupedItem> map = new LinkedHashMap<>();

        for (BillingItemModel item : items) {

            String key = item.getName() + "_" + item.getUnits();

            if (!map.containsKey(key)) {
                map.put(key, new GroupedItem(item.getName(), item.getUnits(),item.getSellingItemPrice()));
            }

            GroupedItem grouped = map.get(key);
            grouped.count++;
            grouped.totalSellingPrice += item.getSellingItemPrice();
        }

        return new ArrayList<>(map.values());
    }



}


