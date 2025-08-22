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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;

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

        // Products
        paint.setTextAlign(Paint.Align.LEFT);
        for (BillingItemModel p : billData.getBillingItemModelList()) {
            String name = (p.getName().length() > 20 ? p.getName().substring(0, 20) : p.getName());
            String qty = p.getUnits() != null ? p.getUnits() + " ML" : "";
            String price = "Rs." + String.format("%.2f", p.getSellingItemPrice());

            canvas.drawText(name, 10, y, paint);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(qty, pageWidth / 2f, y, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(price, pageWidth - 20, y, paint);
            y += 30;
            paint.setTextAlign(Paint.Align.LEFT);
        }

        // Separator
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(dashLine, 0, y, paint);
        y += 30;

        // Totals
        paint.setTextAlign(Paint.Align.CENTER);
        if (billData.getDiscount() > 0) {
            canvas.drawText("Bill Amount: Rs." + String.format("%.2f", billData.getTotalCost()), pageWidth / 2f, y, paint);
            y += 30;
            canvas.drawText("Discount: " + String.format("%.1f", billData.getDiscount()) + "%", pageWidth / 2f, y, paint);
            y += 30;
        }

        if (billData.getIsCourier() != null && billData.getIsCourier()) {
            canvas.drawText("Courier Charge: " + String.format("%.1f", billData.getCourierAmount()) + "%", pageWidth / 2f, y, paint);
            y += 30;
            double sellingWithCourier = billData.getSellingCost() + billData.getCourierAmount();
            paint.setTextSize(22f);
            canvas.drawText("Total: Rs." + String.format("%.2f", sellingWithCourier), pageWidth / 2f, y, paint);
            y += 40;
        } else {
            paint.setTextSize(22f);
            canvas.drawText("Total: Rs." + String.format("%.2f", billData.getSellingCost()), pageWidth / 2f, y, paint);
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

}
