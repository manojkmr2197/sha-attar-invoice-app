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
     * Compute item level GST details
     */
    public static void populateItemGST(BillingItemModel item) {
        if (item == null) return;
        double itemPrice = item.getSellingItemPrice() * item.getPieces();
        double gstRate = item.getGstPercentage() > 0 ? item.getGstPercentage() : DEFAULT_PERFUME_GST;
        item.setGstPercentage(gstRate);
        
        // Taxable amount before GST
        double gstAmount = round((itemPrice * gstRate) / 100.0);
        item.setTaxableValue(itemPrice);
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
            invoice.setTaxableAmount(invoice.getSellingCost());
            invoice.setRoundOff(0.0);
            return;
        }

        double taxableAmount = invoice.getSellingCost();
        invoice.setTaxableAmount(taxableAmount);

        boolean insideTN = isInsideTN(invoice.getPlaceOfSupply());

        // Default to 18% total GST rate (CGST 9% + SGST 9% or IGST 18%)
        double totalGstRate = DEFAULT_PERFUME_GST;
        
        // If items are present, compute average or highest rate
        List<BillingItemModel> items = invoice.getBillingItemModelList();
        if (items != null && !items.isEmpty()) {
            double weightedTax = 0.0;
            double totalItemCost = 0.0;
            for (BillingItemModel item : items) {
                double itemTotal = item.getSellingItemPrice() * item.getPieces();
                double rate = item.getGstPercentage() > 0 ? item.getGstPercentage() : DEFAULT_PERFUME_GST;
                item.setGstPercentage(rate);
                weightedTax += (itemTotal * (rate / 100.0));
                totalItemCost += itemTotal;
            }
            if (totalItemCost > 0) {
                totalGstRate = (weightedTax / totalItemCost) * 100.0;
            }
        }

        if (insideTN) {
            double halfRate = totalGstRate / 2.0;
            double cgst = round(taxableAmount * (halfRate / 100.0));
            double sgst = round(taxableAmount * (halfRate / 100.0));
            invoice.setCgstAmount(cgst);
            invoice.setSgstAmount(sgst);
            invoice.setIgstAmount(0.0);
        } else {
            double igst = round(taxableAmount * (totalGstRate / 100.0));
            invoice.setCgstAmount(0.0);
            invoice.setSgstAmount(0.0);
            invoice.setIgstAmount(igst);
        }

        double totalWithTax = taxableAmount + invoice.getCgstAmount() + invoice.getSgstAmount() + invoice.getIgstAmount();
        if (invoice.getIsCourier() != null && invoice.getIsCourier()) {
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
