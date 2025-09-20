package com.app.sha.attar.invoice.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BluetoothPrinterHelper {

    private Context context;
    private Activity activity;

    public BluetoothPrinterHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public boolean printSmallFontReceipt1(BillingInvoiceModel billData) {
        AlertDialog printingDialog = showPrintingDialog(context);
        try {

            BluetoothConnection printerConnection = BluetoothPrintersConnections.selectFirstPaired();

            if (printerConnection == null) {
                Toast.makeText(context, "No Bluetooth printer found", Toast.LENGTH_SHORT).show();
                return false;
            }
            printingDialog.show();
            // 80mm paper → 72mm printable width → very small font by using 72 chars per line
            EscPosPrinter printer = new EscPosPrinter(printerConnection, 203, 72f, 48);

            // Load logo
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_print_logo);

            // Date in top right corner
            String dateStr = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());
            int lineWidth = 48;
            String separator = new String(new char[lineWidth]).replace('\0', '-');
            String star_separator = new String(new char[lineWidth]).replace('\0', '*');

            // Build product list with 3-column format
            StringBuilder productLines = new StringBuilder();
            //productLines.append(String.format("[L]%-20s %6s %10s\n", "", "", ""));
            for (BillingItemModel p : billData.getBillingItemModelList()) {
                String productType = StringUtils.isNotBlank(p.getProductCategory()) ? "(" + p.getProductCategory().substring(0, 1) + ")" : "";
                String name = (p.getName().length() > 20 ? p.getName().substring(0, 20) : p.getName()) + productType;
                String qty = p.getUnits() != null ? p.getUnits() + " ML" : "";
                String price = "Rs." + String.format("%.2f", p.getSellingItemPrice());

                productLines.append(String.format("[L]%-24s %8s %12s\n", name, qty, price));
                //productLines.append(String.format("[L]%-20s %6s %10s\n", name, qty, price));
            }
            //productLines.append(String.format("[L]%-20s %6s %10s\n", "", "", ""));

            StringBuilder receipt = new StringBuilder();

// Logo
            receipt.append("[C]<img>")
                    .append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logo))
                    .append("</img>\n");


            receipt.append("[C]<b>SHA'S ATTAR & PERFUMES")
                    .append("</b>\n");

            receipt.append("[C]<font name='b'>(Make your own Perfume)</font>")
                    .append("\n\n");

            // bill Date
            receipt.append("[R]Date: ")
                    .append(dateStr.toUpperCase())
                    .append("\n\n");

            // Header: Centered, bold, underline
            receipt.append("[L]<u><b>ORDER No: ")
                    .append(billData.getBillingDate())
                    .append("</b></u>\n");

            receipt.append("[L]<b>Bill by: </b>")
                    .append(billData.getCustomerName().toUpperCase())
                    .append("\n");


// Separator
            receipt.append("[L]").append(separator).append("\n");
            receipt.append(String.format("[L]%-24s %8s %12s\n", "PRODUCT", "QTY", "PRICE"));
            receipt.append("[L]").append(separator).append("\n");
// Product lines in small font
            receipt.append(productLines.toString());
            receipt.append("[L]").append(separator).append("\n");

// Totals (Right aligned)
            if (billData.getDiscount() > 0) {
                receipt.append("[C]<b>Bill Amount:[R]").append("Rs.").append(String.format("%.2f", billData.getTotalCost())).append("  </b>\n");
                receipt.append("[C]<b>Discount:[R]").append(String.format("%.1f", billData.getDiscount())).append("%  </b>\n");
            }
            if (billData.getIsCourier() != null && billData.getIsCourier()) {
                receipt.append("[C]<b>Courier Charge:[R]").append(String.format("%.1f", billData.getCourierAmount())).append("%  </b>\n");
                double sellingWithCourier = billData.getSellingCost() + billData.getCourierAmount();
                receipt.append("[C]<b><font size='big'>Total:[R]").append("<u>Rs.").append(String.format("%.2f", sellingWithCourier)).append("</u></font></b>  \n\n");

            } else {
                receipt.append("[C]<b><font size='big'>Total:[R]").append("<u>Rs.").append(String.format("%.2f", billData.getSellingCost())).append("</u></font></b>  \n\n");
            }
            receipt.append("[L]Payment: ")
                    .append(billData.getPaymentMode())
                    .append("\n");
            receipt.append("[L]").append(star_separator).append("\n");
// Footer: Centered thank you

            receipt.append("[L]A11, Gemini Parson Complex, Basement Floor, \n" +
                    "[L]Kodambakkam High Road, Nungambakkam.\n" +
                    "[L]Chennai-600006\n");

            receipt.append("[C]Phone No : +91 978 977 5134\n");
            receipt.append("[L]").append(star_separator).append("\n");
            receipt.append("[C]Thank you for shopping with us!\n");
            receipt.append("[C]**ALL SALES ARE FINAL**\n");
            receipt.append("[L]").append(star_separator).append("\n");
// Print
            new Thread(() -> {
                try {
                    printer.printFormattedTextAndCut(receipt.toString());
                    printingDialog.dismiss();
                    Toast.makeText(context, "Printing Success .!", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Toast.makeText(context, "Printer not available. Please restart the printer.!", Toast.LENGTH_LONG).show();
                }
            }).start();
            //printer.printFormattedTextAndCut(receipt.toString());

            return true;
        } catch (Exception e) {
            Toast.makeText(context, "Printing Failed .!", Toast.LENGTH_SHORT).show();
            printingDialog.dismiss();
            return false;
        }
    }


    public boolean printSmallFontReceiptBkp(BillingInvoiceModel billData) {
        AlertDialog printingDialog = showPrintingDialog(context);
        printingDialog.show();

// Run everything in background
        new Thread(() -> {
            try {
                BluetoothConnection printerConnection = BluetoothPrintersConnections.selectFirstPaired();

                if (printerConnection == null) {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(context, "Printer not available. Please restart the printer.!", Toast.LENGTH_SHORT).show();
                        printingDialog.dismiss();
                    });
                    return;
                }

                EscPosPrinter printer = new EscPosPrinter(printerConnection, 203, 72f, 48);

                // Heavy operations moved to background
                Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_print_logo);
                String dateStr = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());
                int lineWidth = 48;
                String separator = new String(new char[lineWidth]).replace('\0', '-');
                String star_separator = new String(new char[lineWidth]).replace('\0', '*');

                StringBuilder productLines = new StringBuilder();
                for (BillingItemModel p : billData.getBillingItemModelList()) {
                    String productType = StringUtils.isNotBlank(p.getProductCategory()) ? "(" + p.getProductCategory().substring(0, 1) + ")" : "";
                    String name = (p.getName().length() > 20 ? p.getName().substring(0, 20) : p.getName()) + productType;
                    String qty = p.getUnits() != null ? p.getUnits() + " ML" : "";
                    String price = "Rs." + String.format("%.2f", p.getSellingItemPrice());
                    productLines.append(String.format("[L]%-24s %8s %12s\n", name, qty, price));
                }

                StringBuilder receipt = new StringBuilder();
                receipt.append("[C]<img>").append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logo)).append("</img>\n");
                receipt.append("[C]<b>SHA'S ATTAR & PERFUMES</b>\n");
                receipt.append("[C]<font name='b'>(Make your own Perfume)</font>\n\n");
                receipt.append("[R]Date: ").append(dateStr.toUpperCase()).append("\n\n");
                receipt.append("[L]<u><b>ORDER No: ").append(billData.getBillingDate()).append("</b></u>\n");
                receipt.append("[L]<b>Bill by: </b>").append(billData.getCustomerName().toUpperCase()).append("\n");
                receipt.append("[L]").append(separator).append("\n");
                receipt.append(String.format("[L]%-24s %8s %12s\n", "PRODUCT", "QTY", "PRICE"));
                receipt.append("[L]").append(separator).append("\n");
                receipt.append(productLines.toString());
                receipt.append("[L]").append(separator).append("\n");

                if (billData.getDiscount() > 0) {
                    receipt.append("[C]<b>Bill Amount:[R]").append("Rs.").append(String.format("%.2f", billData.getTotalCost())).append("  </b>\n");
                    receipt.append("[C]<b>Discount:[R]").append(String.format("%.1f", billData.getDiscount())).append("%  </b>\n");
                }

                receipt.append("[C]<b><font size='big'>Total:[R]").append("<u>Rs.").append(String.format("%.2f", billData.getSellingCost())).append("</u></font></b>  \n\n");
                receipt.append("[L]Payment: ").append(billData.getPaymentMode()).append("\n");
                receipt.append("[L]").append(star_separator).append("\n");
                receipt.append("[L]A11, Gemini Parson Complex, Basement Floor,\n");
                receipt.append("[L]Kodambakkam High Road, Nungambakkam.\n");
                receipt.append("[L]Chennai-600006\n");
                receipt.append("[C]Phone No : +91 978 977 5134\n");
                receipt.append("[L]").append(star_separator).append("\n");
                receipt.append("[C]Thank you for shopping with us!\n");
                receipt.append("[C]**All sales are final**\n");
                receipt.append("[L]").append(star_separator).append("\n");

                printer.printFormattedTextAndCut(receipt.toString());

                activity.runOnUiThread(() -> {
                    printingDialog.dismiss();
                    Toast.makeText(context, "Printing Success!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    printingDialog.dismiss();
                    Toast.makeText(context, "Printing Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
        return true;
    }

    public void printSmallFontReceipt(BillingInvoiceModel billData) {
        AlertDialog printingDialog = showPrintingDialog(context);
        printingDialog.show();


        try {
            BluetoothConnection printerConnection = BluetoothPrintersConnections.selectFirstPaired();

            if (printerConnection == null) {
                Toast.makeText(context, "Printer not available. Please restart the printer.!", Toast.LENGTH_SHORT).show();
                printingDialog.dismiss();
                return;
            }

            EscPosPrinter printer = new EscPosPrinter(printerConnection, 203, 72f, 48);

            // Heavy operations moved to background
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_print_logo);
            String dateStr = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());
            int lineWidth = 48;
            String separator = new String(new char[lineWidth]).replace('\0', '-');
            String star_separator = new String(new char[lineWidth]).replace('\0', '*');

            StringBuilder productLines = new StringBuilder();
            for (BillingItemModel p : billData.getBillingItemModelList()) {
                String productType = StringUtils.isNotBlank(p.getProductCategory()) ? "(" + p.getProductCategory().substring(0, 1) + ")" : "";
                String name = (p.getName().length() > 20 ? p.getName().substring(0, 20) : p.getName()) + productType;
                String qty = p.getUnits() != null ? p.getUnits() + " ML" : "";
                String price = "Rs." + String.format("%.2f", p.getSellingItemPrice());
                productLines.append(String.format("[L]%-24s %8s %12s\n", name, qty, price));
            }

            StringBuilder receipt = new StringBuilder();
            receipt.append("[C]<img>").append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logo)).append("</img>\n");
            receipt.append("[C]<b>SHA'S ATTAR & PERFUMES</b>\n");
            receipt.append("[C]<font name='b'>(Make your own Perfume)</font>\n\n");
            receipt.append("[R]Date: ").append(dateStr.toUpperCase()).append("\n\n");
            receipt.append("[L]<u><b>ORDER No: ").append(billData.getBillingDate()).append("</b></u>\n");
            receipt.append("[L]<b>Bill by: </b>").append(billData.getCustomerName().toUpperCase()).append("\n");
            receipt.append("[L]").append(separator).append("\n");
            receipt.append(String.format("[L]%-24s %8s %12s\n", "PRODUCT", "QTY", "PRICE"));
            receipt.append("[L]").append(separator).append("\n");
            receipt.append(productLines.toString());
            receipt.append("[L]").append(separator).append("\n");

            if (billData.getDiscount() > 0) {
                receipt.append("[C]<b>Bill Amount:[R]").append("Rs.").append(String.format("%.2f", billData.getTotalCost())).append("  </b>\n");
                receipt.append("[C]<b>Discount:[R]").append(String.format("%.1f", billData.getDiscount())).append("%  </b>\n");
            }

            receipt.append("[C]<b><font size='big'>Total:[R]").append("<u>Rs.").append(String.format("%.2f", billData.getSellingCost())).append("</u></font></b>  \n\n");
            receipt.append("[L]Payment: ").append(billData.getPaymentMode()).append("\n");
            receipt.append("[L]").append(star_separator).append("\n");
            receipt.append("[L]A11, Gemini Parson Complex, Basement Floor,\n");
            receipt.append("[L]Kodambakkam High Road, Nungambakkam.\n");
            receipt.append("[L]Chennai-600006\n");
            receipt.append("[C]Phone No : +91 978 977 5134\n");
            receipt.append("[L]").append(star_separator).append("\n");
            receipt.append("[C]Thank you for shopping with us!\n");
            receipt.append("[C]**All sales are final**\n");
            receipt.append("[L]").append(star_separator).append("\n");

            printer.printFormattedTextAndCut(receipt.toString());

            printingDialog.dismiss();
            Toast.makeText(context, "Printing Success!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            printingDialog.dismiss();
            Toast.makeText(context, "Printing Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    // Create a method to show the printing progress dialog
    private AlertDialog showPrintingDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false); // prevent closing manually

        // Inflate custom layout (optional)
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setGravity(Gravity.CENTER_VERTICAL);

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        layout.addView(progressBar);

        TextView message = new TextView(context);
        message.setText("Printing in-progress...\nPlease wait");
        message.setTextSize(16);
        message.setPadding(30, 0, 0, 0);
        layout.addView(message);

        builder.setView(layout);

        return builder.create();

    }

}
