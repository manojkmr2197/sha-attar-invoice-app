package com.app.sha.attar.invoice.utils;

import android.content.Context;
import android.os.Environment;

import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GSTReportExcelHelper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final ZoneOffset IST_OFFSET = ZoneOffset.ofHoursMinutes(5, 30);

    public static File generateGSTExcelReport(Context context,
                                             List<BillingInvoiceModel> invoiceList,
                                             OffsetDateTime startDate,
                                             OffsetDateTime endDate,
                                             boolean gstOnly) throws Exception {

        Workbook workbook = new XSSFWorkbook();

        // 1. Prepare filtered list with robust null checking
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

        // 2. Cell Styles
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle totalStyle = createTotalStyle(workbook);
        CellStyle totalNumberStyle = createTotalNumberStyle(workbook);
        CellStyle sectionHeaderStyle = createSectionHeaderStyle(workbook);

        // 3. Create Sheet 1: GST Sales Register
        createSalesRegisterSheet(workbook, filteredInvoices, startDate, endDate,
                titleStyle, subtitleStyle, headerStyle, dataStyle, numberStyle, totalStyle, totalNumberStyle);

        // 4. Create Sheet 2: GSTR Summary
        createGSTRSummarySheet(workbook, filteredInvoices, startDate, endDate,
                titleStyle, subtitleStyle, sectionHeaderStyle, headerStyle, dataStyle, numberStyle, totalStyle, totalNumberStyle);

        // 5. Write to file in Downloads
        String dateStr = (startDate != null && endDate != null)
                ? startDate.format(DATE_FORMATTER) + "_to_" + endDate.format(DATE_FORMATTER)
                : String.valueOf(System.currentTimeMillis());

        String fileName = "GST_Sales_Report_" + dateStr + ".xlsx";
        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File file = new File(exportDir, fileName);

        FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

        return file;
    }

    private static void createSalesRegisterSheet(Workbook workbook,
                                                List<BillingInvoiceModel> invoices,
                                                OffsetDateTime startDate,
                                                OffsetDateTime endDate,
                                                CellStyle titleStyle,
                                                CellStyle subtitleStyle,
                                                CellStyle headerStyle,
                                                CellStyle dataStyle,
                                                CellStyle numberStyle,
                                                CellStyle totalStyle,
                                                CellStyle totalNumberStyle) {

        Sheet sheet = workbook.createSheet("GST Sales Register");
        sheet.setDisplayGridlines(true);

        int rowIdx = 0;

        // Title Row
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("SHA'S ATTAR AND PERFUMES - GST SALES REGISTER (GSTR-1 FORMAT)");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 16));

        // Period Subtitle
        String periodText = "GSTIN: 33FIPPM7687P1ZZ | Report Period: " +
                (startDate != null ? startDate.format(DATE_FORMATTER) : "N/A") + " to " +
                (endDate != null ? endDate.format(DATE_FORMATTER) : "N/A") +
                " | Generated on: " + OffsetDateTime.now().atZoneSameInstant(IST_OFFSET).format(DATE_FORMATTER);
        Row subRow = sheet.createRow(rowIdx++);
        Cell subCell = subRow.createCell(0);
        subCell.setCellValue(periodText);
        subCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 16));

        rowIdx++; // Blank row

        // Headers
        String[] headers = {
                "S.No", "Invoice Date", "Invoice ID", "Customer Name", "Customer Phone",
                "Customer GSTIN", "Place of Supply", "Taxable Value (₹)",
                "CGST (₹)", "SGST (₹)", "IGST (₹)", "Total Tax (₹)",
                "Courier (₹)", "Round Off (₹)", "Invoice Value (₹)", "Payment Mode", "GST Type"
        };

        Row headerRow = sheet.createRow(rowIdx++);
        headerRow.setHeightInPoints(24);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Totals accumulators
        double sumTaxable = 0.0;
        double sumCgst = 0.0;
        double sumSgst = 0.0;
        double sumIgst = 0.0;
        double sumTotalTax = 0.0;
        double sumCourier = 0.0;
        double sumRoundOff = 0.0;
        double sumGrandTotal = 0.0;

        int serial = 1;
        for (BillingInvoiceModel inv : invoices) {
            if (inv == null) continue;

            Row row = sheet.createRow(rowIdx++);

            // Safe values extraction with 0 defaults
            String invDate = "N/A";
            if (inv.getBillingDate() != null) {
                try {
                    OffsetDateTime dt = Instant.ofEpochSecond(inv.getBillingDate()).atOffset(IST_OFFSET);
                    invDate = dt.format(DATE_FORMATTER);
                } catch (Exception ignored) {}
            }

            String invId = inv.getBillingDate() != null ? String.valueOf(inv.getBillingDate()) : "N/A";
            String custName = StringUtils.isNotBlank(inv.getClientName()) ? inv.getClientName() :
                    (StringUtils.isNotBlank(inv.getCustomerName()) ? inv.getCustomerName() : "Walk-in Customer");
            String custPhone = StringUtils.isNotBlank(inv.getClientPhoneNo()) ? inv.getClientPhoneNo() :
                    (StringUtils.isNotBlank(inv.getCustomerPhone()) ? inv.getCustomerPhone() : "");
            String custGst = StringUtils.isNotBlank(inv.getCustomerGST()) ? inv.getCustomerGST() : "-";
            String placeOfSupply = StringUtils.isNotBlank(inv.getPlaceOfSupply()) ? inv.getPlaceOfSupply() : "Inside TN";

            boolean isGst = inv.getIsGSTApplicable() != null && inv.getIsGSTApplicable();
            double sellingCost = inv.getSellingCost() != null ? inv.getSellingCost() : 0.0;
            double taxable = inv.getTaxableAmount() != null && inv.getTaxableAmount() > 0 ? inv.getTaxableAmount() : (isGst ? sellingCost : sellingCost);
            double cgst = inv.getCgstAmount() != null ? inv.getCgstAmount() : 0.0;
            double sgst = inv.getSgstAmount() != null ? inv.getSgstAmount() : 0.0;
            double igst = inv.getIgstAmount() != null ? inv.getIgstAmount() : 0.0;
            double totalTax = cgst + sgst + igst;
            double courier = inv.getCourierAmount() != null ? inv.getCourierAmount() : 0.0;
            double roundOff = inv.getRoundOff() != null ? inv.getRoundOff() : 0.0;
            double grandTotal = inv.getGrandTotal() != null ? inv.getGrandTotal() : (sellingCost + totalTax + courier + roundOff);
            String payMode = StringUtils.isNotBlank(inv.getPaymentMode()) ? inv.getPaymentMode() : "CASH";
            String gstType = isGst ? (StringUtils.isNotBlank(inv.getCustomerGST()) ? "B2B" : "B2C") : "Non-GST";

            // Populate cells
            createCell(row, 0, String.valueOf(serial++), dataStyle);
            createCell(row, 1, invDate, dataStyle);
            createCell(row, 2, invId, dataStyle);
            createCell(row, 3, custName, dataStyle);
            createCell(row, 4, custPhone, dataStyle);
            createCell(row, 5, custGst, dataStyle);
            createCell(row, 6, placeOfSupply, dataStyle);

            createNumberCell(row, 7, taxable, numberStyle);
            createNumberCell(row, 8, cgst, numberStyle);
            createNumberCell(row, 9, sgst, numberStyle);
            createNumberCell(row, 10, igst, numberStyle);
            createNumberCell(row, 11, totalTax, numberStyle);
            createNumberCell(row, 12, courier, numberStyle);
            createNumberCell(row, 13, roundOff, numberStyle);
            createNumberCell(row, 14, grandTotal, numberStyle);
            createCell(row, 15, payMode, dataStyle);
            createCell(row, 16, gstType, dataStyle);

            // Accumulate
            sumTaxable += taxable;
            sumCgst += cgst;
            sumSgst += sgst;
            sumIgst += igst;
            sumTotalTax += totalTax;
            sumCourier += courier;
            sumRoundOff += roundOff;
            sumGrandTotal += grandTotal;
        }

        // Summary Total Row
        Row totalRow = sheet.createRow(rowIdx);
        totalRow.setHeightInPoints(22);

        Cell totLabelCell = totalRow.createCell(0);
        totLabelCell.setCellValue("TOTAL (" + (serial - 1) + " Invoices)");
        totLabelCell.setCellStyle(totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 6));

        for (int c = 1; c <= 6; c++) {
            Cell blankCell = totalRow.createCell(c);
            blankCell.setCellStyle(totalStyle);
        }

        createNumberCell(totalRow, 7, sumTaxable, totalNumberStyle);
        createNumberCell(totalRow, 8, sumCgst, totalNumberStyle);
        createNumberCell(totalRow, 9, sumSgst, totalNumberStyle);
        createNumberCell(totalRow, 10, sumIgst, totalNumberStyle);
        createNumberCell(totalRow, 11, sumTotalTax, totalNumberStyle);
        createNumberCell(totalRow, 12, sumCourier, totalNumberStyle);
        createNumberCell(totalRow, 13, sumRoundOff, totalNumberStyle);
        createNumberCell(totalRow, 14, sumGrandTotal, totalNumberStyle);

        Cell blankPay = totalRow.createCell(15);
        blankPay.setCellValue("");
        blankPay.setCellStyle(totalStyle);

        Cell blankType = totalRow.createCell(16);
        blankType.setCellValue("");
        blankType.setCellStyle(totalStyle);

        // Set explicit column widths (in 1/256th of character width)
        int[] columnWidths = {
                1800,  // 0: S.No
                3600,  // 1: Invoice Date
                4200,  // 2: Invoice ID
                6200,  // 3: Customer Name
                4200,  // 4: Customer Phone
                4800,  // 5: Customer GSTIN
                4200,  // 6: Place of Supply
                4200,  // 7: Taxable Value
                3400,  // 8: CGST
                3400,  // 9: SGST
                3400,  // 10: IGST
                4000,  // 11: Total Tax
                3400,  // 12: Courier
                3400,  // 13: Round Off
                4500,  // 14: Invoice Value
                3400,  // 15: Payment Mode
                3200   // 16: GST Type
        };

        for (int i = 0; i < columnWidths.length; i++) {
            sheet.setColumnWidth(i, columnWidths[i]);
        }
    }

    private static void createGSTRSummarySheet(Workbook workbook,
                                              List<BillingInvoiceModel> invoices,
                                              OffsetDateTime startDate,
                                              OffsetDateTime endDate,
                                              CellStyle titleStyle,
                                              CellStyle subtitleStyle,
                                              CellStyle sectionHeaderStyle,
                                              CellStyle headerStyle,
                                              CellStyle dataStyle,
                                              CellStyle numberStyle,
                                              CellStyle totalStyle,
                                              CellStyle totalNumberStyle) {

        Sheet sheet = workbook.createSheet("GSTR-1 Tax Summary");
        sheet.setDisplayGridlines(true);

        int rowIdx = 0;

        // Title
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("SHA'S ATTAR AND PERFUMES- AUDITOR GST SUMMARY STATEMENT");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        // Subtitle
        String periodText = "GSTIN: 33FIPPM7687P1ZZ | Period: " +
                (startDate != null ? startDate.format(DATE_FORMATTER) : "N/A") + " to " +
                (endDate != null ? endDate.format(DATE_FORMATTER) : "N/A");
        Row subRow = sheet.createRow(rowIdx++);
        Cell subCell = subRow.createCell(0);
        subCell.setCellValue(periodText);
        subCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

        rowIdx++; // Blank row

        // ==========================================
        // 1. RATE-WISE BREAKDOWN TABLE
        // ==========================================
        Row sec1Row = sheet.createRow(rowIdx++);
        Cell sec1Cell = sec1Row.createCell(0);
        sec1Cell.setCellValue("1. RATE-WISE GST TURNOVER BREAKDOWN");
        sec1Cell.setCellStyle(sectionHeaderStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 6));

        String[] rateHeaders = {"Tax Rate", "Description / HSN", "Taxable Value (₹)", "CGST (₹)", "SGST (₹)", "IGST (₹)", "Total Tax (₹)"};
        Row rateHeadRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < rateHeaders.length; i++) {
            Cell c = rateHeadRow.createCell(i);
            c.setCellValue(rateHeaders[i]);
            c.setCellStyle(headerStyle);
        }

        // Dynamic Rate Map for arbitrary tax rates & HSN codes
        class TaxRateSummary {
            String rateLabel;
            String descHsn;
            double taxable = 0.0;
            double cgst = 0.0;
            double sgst = 0.0;
            double igst = 0.0;

            TaxRateSummary(String rateLabel, String descHsn) {
                this.rateLabel = rateLabel;
                this.descHsn = descHsn;
            }
        }
        java.util.Map<String, TaxRateSummary> rateMap = new java.util.LinkedHashMap<>();

        for (BillingInvoiceModel inv : invoices) {
            if (inv == null) continue;
            boolean isGst = inv.getIsGSTApplicable() != null && inv.getIsGSTApplicable();
            if (!isGst) continue;

            boolean isInsideTn = GSTCalculator.isInsideTN(inv.getPlaceOfSupply());

            if (inv.getBillingItemModelList() != null && !inv.getBillingItemModelList().isEmpty()) {
                double invoiceDiscount = inv.getDiscount() != null ? inv.getDiscount() : 0.0;
                for (BillingItemModel item : inv.getBillingItemModelList()) {
                    if (item == null) continue;
                    int pieces = item.getPieces() != null && item.getPieces() > 0 ? item.getPieces() : 1;
                    double unitPrice = item.getSellingItemPrice() != null ? item.getSellingItemPrice() : 0.0;
                    double itemTotal = unitPrice * pieces;
                    double itemTaxable = itemTotal - (itemTotal * invoiceDiscount / 100.0);

                    double rate = item.getGstPercentage() > 0 ? item.getGstPercentage() : ("NON_PRODUCT".equalsIgnoreCase(item.getType()) ? 12.0 : 18.0);
                    String hsn = StringUtils.isNotBlank(item.getHsnCode()) ? item.getHsnCode() : ("NON_PRODUCT".equalsIgnoreCase(item.getType()) ? "96161000" : "33029090");
                    String rateKey = String.format(Locale.ENGLISH, "%.1f_%s", rate, hsn);

                    TaxRateSummary summary = rateMap.computeIfAbsent(rateKey, k -> {
                        String label = String.format(Locale.ENGLISH, "%.0f%% GST", rate);
                        String desc = ("NON_PRODUCT".equalsIgnoreCase(item.getType()) ? "Accessories & Packaging" : "Attar / Perfumes") + " (HSN: " + hsn + ")";
                        return new TaxRateSummary(label, desc);
                    });

                    summary.taxable += itemTaxable;
                    if (isInsideTn) {
                        summary.cgst += (itemTaxable * (rate / 2.0)) / 100.0;
                        summary.sgst += (itemTaxable * (rate / 2.0)) / 100.0;
                    } else {
                        summary.igst += (itemTaxable * rate) / 100.0;
                    }
                }
            } else {
                // Fallback from invoice totals if items list empty
                double tax = inv.getTaxableAmount() != null ? inv.getTaxableAmount() : 0.0;
                TaxRateSummary summary = rateMap.computeIfAbsent("18.0_33029090", k -> new TaxRateSummary("18% GST", "Attar / Perfumes (HSN: 33029090)"));
                summary.taxable += tax;
                summary.cgst += inv.getCgstAmount() != null ? inv.getCgstAmount() : 0.0;
                summary.sgst += inv.getSgstAmount() != null ? inv.getSgstAmount() : 0.0;
                summary.igst += inv.getIgstAmount() != null ? inv.getIgstAmount() : 0.0;
            }
        }

        double grandTaxable = 0.0, grandCgst = 0.0, grandSgst = 0.0, grandIgst = 0.0;
        for (TaxRateSummary summary : rateMap.values()) {
            Row r = sheet.createRow(rowIdx++);
            createCell(r, 0, summary.rateLabel, dataStyle);
            createCell(r, 1, summary.descHsn, dataStyle);
            createNumberCell(r, 2, summary.taxable, numberStyle);
            createNumberCell(r, 3, summary.cgst, numberStyle);
            createNumberCell(r, 4, summary.sgst, numberStyle);
            createNumberCell(r, 5, summary.igst, numberStyle);
            createNumberCell(r, 6, summary.cgst + summary.sgst + summary.igst, numberStyle);

            grandTaxable += summary.taxable;
            grandCgst += summary.cgst;
            grandSgst += summary.sgst;
            grandIgst += summary.igst;
        }

        // Total Rate Row
        Row rTot = sheet.createRow(rowIdx++);
        Cell totC = rTot.createCell(0);
        totC.setCellValue("TOTAL GST TURNOVER");
        totC.setCellStyle(totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 1));
        rTot.createCell(1).setCellStyle(totalStyle);
        createNumberCell(rTot, 2, grandTaxable, totalNumberStyle);
        createNumberCell(rTot, 3, grandCgst, totalNumberStyle);
        createNumberCell(rTot, 4, grandSgst, totalNumberStyle);
        createNumberCell(rTot, 5, grandIgst, totalNumberStyle);
        createNumberCell(rTot, 6, (grandCgst + grandSgst + grandIgst), totalNumberStyle);

        rowIdx += 2; // Spacing

        // ==========================================
        // 2. PLACE OF SUPPLY SUMMARY
        // ==========================================
        Row sec2Row = sheet.createRow(rowIdx++);
        Cell sec2Cell = sec2Row.createCell(0);
        sec2Cell.setCellValue("2. SUPPLY TYPE BREAKDOWN");
        sec2Cell.setCellStyle(sectionHeaderStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 4));

        String[] posHeaders = {"Supply Type", "State / Place", "Taxable Turnover (₹)", "Tax Collected (₹)", "Bills Count"};
        Row posHeadRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < posHeaders.length; i++) {
            Cell c = posHeadRow.createCell(i);
            c.setCellValue(posHeaders[i]);
            c.setCellStyle(headerStyle);
        }

        double intraTaxable = 0.0, intraTax = 0.0;
        int intraCount = 0;
        double interTaxable = 0.0, interTax = 0.0;
        int interCount = 0;

        for (BillingInvoiceModel inv : invoices) {
            if (inv == null) continue;
            boolean isGst = inv.getIsGSTApplicable() != null && inv.getIsGSTApplicable();
            if (!isGst) continue;

            double taxAmt = (inv.getCgstAmount() != null ? inv.getCgstAmount() : 0.0) +
                    (inv.getSgstAmount() != null ? inv.getSgstAmount() : 0.0) +
                    (inv.getIgstAmount() != null ? inv.getIgstAmount() : 0.0);
            double taxable = inv.getTaxableAmount() != null ? inv.getTaxableAmount() : 0.0;

            if (GSTCalculator.isInsideTN(inv.getPlaceOfSupply())) {
                intraTaxable += taxable;
                intraTax += taxAmt;
                intraCount++;
            } else {
                interTaxable += taxable;
                interTax += taxAmt;
                interCount++;
            }
        }

        Row posIntra = sheet.createRow(rowIdx++);
        createCell(posIntra, 0, "Intra-State (CGST + SGST)", dataStyle);
        createCell(posIntra, 1, "Tamil Nadu (33)", dataStyle);
        createNumberCell(posIntra, 2, intraTaxable, numberStyle);
        createNumberCell(posIntra, 3, intraTax, numberStyle);
        createCell(posIntra, 4, String.valueOf(intraCount), dataStyle);

        Row posInter = sheet.createRow(rowIdx++);
        createCell(posInter, 0, "Inter-State (IGST)", dataStyle);
        createCell(posInter, 1, "Outside Tamil Nadu", dataStyle);
        createNumberCell(posInter, 2, interTaxable, numberStyle);
        createNumberCell(posInter, 3, interTax, numberStyle);
        createCell(posInter, 4, String.valueOf(interCount), dataStyle);

        rowIdx += 2; // Spacing

        // ==========================================
        // 3. PAYMENT MODE BREAKDOWN
        // ==========================================
        Row sec3Row = sheet.createRow(rowIdx++);
        Cell sec3Cell = sec3Row.createCell(0);
        sec3Cell.setCellValue("3. PAYMENT MODE SUMMARY");
        sec3Cell.setCellStyle(sectionHeaderStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 3));

        String[] payHeaders = {"Payment Mode", "Invoice Count", "Gross Value Collected (₹)", "Share (%)"};
        Row payHeadRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < payHeaders.length; i++) {
            Cell c = payHeadRow.createCell(i);
            c.setCellValue(payHeaders[i]);
            c.setCellStyle(headerStyle);
        }

        double cashTotal = 0.0, upiTotal = 0.0, cardTotal = 0.0;
        int cashCount = 0, upiCount = 0, cardCount = 0;

        for (BillingInvoiceModel inv : invoices) {
            if (inv == null) continue;
            double grand = inv.getGrandTotal() != null ? inv.getGrandTotal() : 0.0;
            String mode = StringUtils.isNotBlank(inv.getPaymentMode()) ? inv.getPaymentMode() : "CASH";

            if ("UPI".equalsIgnoreCase(mode)) {
                upiTotal += grand;
                upiCount++;
            } else if ("CARD".equalsIgnoreCase(mode)) {
                cardTotal += grand;
                cardCount++;
            } else {
                cashTotal += grand;
                cashCount++;
            }
        }

        double overallGrand = cashTotal + upiTotal + cardTotal;

        Row rCash = sheet.createRow(rowIdx++);
        createCell(rCash, 0, "CASH", dataStyle);
        createCell(rCash, 1, String.valueOf(cashCount), dataStyle);
        createNumberCell(rCash, 2, cashTotal, numberStyle);
        createCell(rCash, 3, String.format(Locale.ENGLISH, "%.1f%%", overallGrand > 0 ? (cashTotal / overallGrand * 100.0) : 0.0), dataStyle);

        Row rUpi = sheet.createRow(rowIdx++);
        createCell(rUpi, 0, "UPI / Online", dataStyle);
        createCell(rUpi, 1, String.valueOf(upiCount), dataStyle);
        createNumberCell(rUpi, 2, upiTotal, numberStyle);
        createCell(rUpi, 3, String.format(Locale.ENGLISH, "%.1f%%", overallGrand > 0 ? (upiTotal / overallGrand * 100.0) : 0.0), dataStyle);

        Row rCard = sheet.createRow(rowIdx++);
        createCell(rCard, 0, "Card / POS", dataStyle);
        createCell(rCard, 1, String.valueOf(cardCount), dataStyle);
        createNumberCell(rCard, 2, cardTotal, numberStyle);
        createCell(rCard, 3, String.format(Locale.ENGLISH, "%.1f%%", overallGrand > 0 ? (cardTotal / overallGrand * 100.0) : 0.0), dataStyle);

        Row rPayTot = sheet.createRow(rowIdx);
        Cell totP = rPayTot.createCell(0);
        totP.setCellValue("TOTAL COLLECTIONS");
        totP.setCellStyle(totalStyle);
        createCell(rPayTot, 1, String.valueOf(cashCount + upiCount + cardCount), totalStyle);
        createNumberCell(rPayTot, 2, overallGrand, totalNumberStyle);
        createCell(rPayTot, 3, "100.0%", totalStyle);

        // Set explicit column widths
        int[] summaryColWidths = {
                5600,  // 0: Rate / Type
                8200,  // 1: Description / State
                5200,  // 2: Taxable Value / Turnover
                4200,  // 3: CGST / Tax Collected
                4200,  // 4: SGST / Bills Count
                4200,  // 5: IGST
                4800   // 6: Total Tax
        };

        for (int i = 0; i < summaryColWidths.length; i++) {
            sheet.setColumnWidth(i, summaryColWidths[i]);
        }
    }

    private static void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private static void createNumberCell(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(Math.round(value * 100.0) / 100.0);
        cell.setCellStyle(style);
    }

    // ==========================================
    // STYLE FACTORIES
    // ==========================================
    private static CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createSubtitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private static CellStyle createSectionHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private static CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private static CellStyle createNumberStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private static CellStyle createTotalStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private static CellStyle createTotalNumberStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private static void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
