package com.app.sha.attar.invoice.utils;

import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;

import java.util.List;

public class GSTCalculator {

    public static final String PLACE_INSIDE_TN = "Inside TN";
    public static final String PLACE_OUTSIDE_TN = "Outside TN";

    public static final double DEFAULT_PERFUME_GST = 18.0;
    public static final double DEFAULT_BAKHOOR_GST = 12.0;

    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static boolean isInsideTN(String placeOfSupply) {
        if (placeOfSupply == null || placeOfSupply.trim().isEmpty()) {
            return true; // Default is Inside TN
        }
        return placeOfSupply.equalsIgnoreCase(PLACE_INSIDE_TN) || 
               placeOfSupply.toLowerCase().contains("inside") || 
               placeOfSupply.toLowerCase().contains("tamil nadu") || 
               placeOfSupply.equalsIgnoreCase("TN");
    }

    /**
     * Compute item level GST details without discount
     */
    public static void populateItemGST(BillingItemModel item) {
        populateItemGST(item, 0.0);
    }

    /**
     * Compute item level GST details taking into account invoice discount
     */
    public static void populateItemGST(BillingItemModel item, double discountPercentage) {
        if (item == null) return;
        int pieces = item.getPieces() != null ? item.getPieces() : 1;
        double unitPrice = item.getSellingItemPrice() != null ? item.getSellingItemPrice() : 0.0;
        double itemPrice = unitPrice * pieces;

        // Determine GST rate - use item's GST percentage if set, otherwise determine by type
        double gstRate = item.getGstPercentage() != null ? item.getGstPercentage() : 0.0;
        if (gstRate <= 0) {
            // Fallback: determine based on item type
            if ("NON_PRODUCT".equalsIgnoreCase(item.getType())) {
                gstRate = DEFAULT_BAKHOOR_GST; // 12% for accessories
            } else {
                gstRate = DEFAULT_PERFUME_GST; // 18% for products
            }
        }
        item.setGstPercentage(gstRate);

        // Calculate taxable value after proportional invoice discount
        double discountFactor = (discountPercentage > 0 && discountPercentage <= 100) ? (1.0 - (discountPercentage / 100.0)) : 1.0;
        double taxableValue = round(itemPrice * discountFactor);
        double gstAmount = round((taxableValue * gstRate) / 100.0);

        item.setTaxableValue(taxableValue);
        item.setGstAmount(gstAmount);
    }

    /**
     * Calculates and populates all GST fields for the invoice
     */
    public static void calculateInvoiceTaxes(BillingInvoiceModel invoice) {
        if (invoice == null) return;

        if (invoice.getIsGSTApplicable() == null || !invoice.getIsGSTApplicable()) {
            invoice.setIsGSTApplicable(false);
            invoice.setCgstAmount(0.0);
            invoice.setSgstAmount(0.0);
            invoice.setIgstAmount(0.0);
            invoice.setTaxableAmount(invoice.getSellingCost() != null ? invoice.getSellingCost() : 0.0);
            invoice.setRoundOff(0.0);
            return;
        }

        double taxableAmount = invoice.getSellingCost() != null ? invoice.getSellingCost() : 0.0;
        invoice.setTaxableAmount(taxableAmount);

        boolean insideTN = isInsideTN(invoice.getPlaceOfSupply());
        double discountPercentage = invoice.getDiscount() != null ? invoice.getDiscount() : 0.0;

        List<BillingItemModel> items = invoice.getBillingItemModelList();
        double totalGstAmount = 0.0;

        if (items != null && !items.isEmpty()) {
            for (BillingItemModel item : items) {
                populateItemGST(item, discountPercentage);
                totalGstAmount += (item.getGstAmount() != null ? item.getGstAmount() : 0.0);
            }
        } else {
            // Fallback when items list is not populated
            totalGstAmount = round(taxableAmount * (DEFAULT_PERFUME_GST / 100.0));
        }
        totalGstAmount = round(totalGstAmount);

        if (insideTN) {
            double cgst = round(totalGstAmount / 2.0);
            double sgst = round(totalGstAmount - cgst);
            invoice.setCgstAmount(cgst);
            invoice.setSgstAmount(sgst);
            invoice.setIgstAmount(0.0);
        } else {
            invoice.setCgstAmount(0.0);
            invoice.setSgstAmount(0.0);
            invoice.setIgstAmount(totalGstAmount);
        }

        double totalWithTax = taxableAmount + totalGstAmount;
        if (invoice.getIsCourier() != null && invoice.getIsCourier() && invoice.getCourierAmount() != null) {
            totalWithTax += invoice.getCourierAmount();
        }

        double roundedGrandTotal = Math.round(totalWithTax);
        double roundOff = round(roundedGrandTotal - totalWithTax);
        invoice.setRoundOff(roundOff);
    }

    private static final String[] units = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] tens = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String convertToWords(double amount) {
        long num = Math.round(amount);
        if (num == 0) return "Zero Rupees Only";
        return convertNumberToWords(num).trim() + " Rupees Only";
    }

    private static String convertNumberToWords(long n) {
        if (n < 0) return "Minus " + convertNumberToWords(-n);
        if (n < 20) return units[(int) n];
        if (n < 100) return tens[(int) (n / 10)] + ((n % 10 != 0) ? " " + units[(int) (n % 10)] : "");
        if (n < 1000) return units[(int) (n / 100)] + " Hundred" + ((n % 100 != 0) ? " " + convertNumberToWords(n % 100) : "");
        if (n < 100000) return convertNumberToWords(n / 1000) + " Thousand" + ((n % 1000 != 0) ? " " + convertNumberToWords(n % 1000) : "");
        if (n < 10000000) return convertNumberToWords(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " + convertNumberToWords(n % 100000) : "");
        return convertNumberToWords(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " + convertNumberToWords(n % 10000000) : "");
    }
}
