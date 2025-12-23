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
import com.app.sha.attar.invoice.model.GroupedItem;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BluetoothPrinterHelper {

    private Context context;
    private Activity activity;

    public BluetoothPrinterHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void printSmallFontReceipt1(BillingInvoiceModel billData) {

        try {
            BluetoothConnection printerConnection = BluetoothPrintersConnections.selectFirstPaired();

            if (printerConnection == null) {
                Toast.makeText(context, "Printer not available. Please restart the printer.!", Toast.LENGTH_SHORT).show();
                //printingDialog.dismiss();
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

            Toast.makeText(context, "Printing Success!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            Toast.makeText(context, "Printing Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void printSmallFontReceipt(BillingInvoiceModel billData) {

        try {
            BluetoothConnection printerConnection =
                    BluetoothPrintersConnections.selectFirstPaired();

            if (printerConnection == null) {
                Toast.makeText(context,
                        "Printer not available. Please restart the printer!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            EscPosPrinter printer =
                    new EscPosPrinter(printerConnection, 203, 72f, 48);

            Bitmap logo = BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.app_print_logo);

            String dateStr = new SimpleDateFormat(
                    "dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());

            int lineWidth = 48;
            String separator = new String(new char[lineWidth]).replace('\0', '-');
            String starSeparator = new String(new char[lineWidth]).replace('\0', '*');

            // =================================================
            // BUILD PRODUCT LINES (MULTILINE SAFE)
            // =================================================
            StringBuilder productLines = new StringBuilder();

            for (BillingItemModel item : billData.getBillingItemModelList()) {

                String displayName = item.getName();

                if (item.getPieces() > 1) {
                    displayName += " [" + item.getPieces() +
                            " x Rs." + String.format("%.2f", item.getSellingItemPrice()) + "]";
                }

                String qty = item.getUnits() != null ? item.getUnits() + " ML" : "";
                String price = "Rs." + String.format("%.2f", item.getPieces() * item.getSellingItemPrice());

                // Split product name into 24-char safe chunks
                List<String> nameLines = splitFixedWidth(displayName, 24);

                // ---- First line (with QTY & PRICE) ----
                productLines.append(
                        String.format("[L]%-24s %8s %12s\n",
                                nameLines.get(0), qty, price)
                );

                // ---- Remaining wrapped lines (NAME ONLY) ----
                for (int i = 1; i < nameLines.size(); i++) {
                    productLines.append(
                            String.format("[L]%-24s\n", nameLines.get(i))
                    );
                }
            }


            // =================================================
            // RECEIPT HEADER
            // =================================================
            StringBuilder receipt = new StringBuilder();

            receipt.append("[C]<img>")
                    .append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logo))
                    .append("</img>\n");

            receipt.append("[C]<b>SHA'S ATTAR & PERFUMES</b>\n");
            receipt.append("[C](Make your own Perfume)\n\n");
            receipt.append("[R]Date: ").append(dateStr.toUpperCase()).append("\n\n");

            receipt.append("[L]<b>ORDER No:</b> ")
                    .append(billData.getBillingDate()).append("\n");

            receipt.append("[L]<b>Bill by:</b> ")
                    .append(billData.getCustomerName().toUpperCase()).append("\n");

            receipt.append("[L]").append(separator).append("\n");
            receipt.append(String.format("[L]%-24s %8s %12s\n",
                    "PRODUCT", "QTY", "PRICE"));
            receipt.append("[L]").append(separator).append("\n");
            receipt.append(productLines);
            receipt.append("[L]").append(separator).append("\n");

            // =================================================
            // TOTALS (PROFESSIONAL LEFT–RIGHT)
            // =================================================
            double sellingCost = billData.getSellingCost();

            receipt.append(String.format(
                    "[L]%-30s [R]Rs.%s\n",
                    "Subtotal",
                    String.format("%.2f", billData.getTotalCost())
            ));

            if (billData.getDiscount() > 0) {
                receipt.append(String.format(
                        "[L]%-30s [R]-Rs.%s\n",
                        "Discount (" + billData.getDiscount() + "%)",
                        String.format("%.2f",
                                billData.getTotalCost() - sellingCost)
                ));
            }

            if (billData.getIsCourier() != null && billData.getIsCourier()) {
                receipt.append(String.format(
                        "[L]%-30s [R]Rs.%s\n",
                        "Courier Charges",
                        String.format("%.2f", billData.getCourierAmount())
                ));
                sellingCost += billData.getCourierAmount();
            }

            receipt.append("[L]").append(separator).append("\n");

            // =================================================
            // CARD PAYMENT SECTION
            // =================================================
            if ("CARD".equalsIgnoreCase(billData.getPaymentMode())) {

                double cardCharge = round(sellingCost * 0.03);
                double gst = round(cardCharge * 0.18);
                double finalPayable = round(sellingCost + cardCharge + gst);

                receipt.append(String.format(
                        "[L]%-30s [R]Rs.%s\n",
                        "Card Charges (3%)",
                        String.format("%.2f", cardCharge)
                ));

                receipt.append(String.format(
                        "[L]%-30s [R]Rs.%s\n",
                        "GST on Card Charges (18%)",
                        String.format("%.2f", gst)
                ));

                receipt.append("[L]").append(separator).append("\n");

                receipt.append(String.format(
                        "[L]<b>%-30s [R]Rs.%s</b>\n",
                        "FINAL PAYABLE",
                        String.format("%.2f", finalPayable)
                ));

            } else {
                receipt.append(String.format(
                        "[L]<b>%-30s [R]Rs.%s</b>\n",
                        "TOTAL",
                        String.format("%.2f", sellingCost)
                ));
            }

            // =================================================
            // FOOTER
            // =================================================
            receipt.append("\n[L]Payment: ")
                    .append(billData.getPaymentMode()).append("\n");

            receipt.append("[L]").append(starSeparator).append("\n");
            receipt.append("[L]A11, Gemini Parson Complex, Basement Floor,\n");
            receipt.append("[L]Kodambakkam High Road, Nungambakkam.\n");
            receipt.append("[L]Chennai-600006\n");
            receipt.append("[C]Phone No : +91 978 977 5134\n");
            receipt.append("[L]").append(starSeparator).append("\n");
            receipt.append("[C]Thank you for shopping with us!\n");
            receipt.append("[C]**ALL SALES ARE FINAL**\n");
            receipt.append("[L]").append(starSeparator).append("\n");

            printer.printFormattedTextAndCut(receipt.toString());

            Toast.makeText(context,
                    "Printing Success!",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(context,
                    "Printing Failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private List<String> splitFixedWidth(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();

        while (text.length() > maxWidth) {
            lines.add(text.substring(0, maxWidth));
            text = text.substring(maxWidth);
        }
        lines.add(text);

        return lines;
    }


}
