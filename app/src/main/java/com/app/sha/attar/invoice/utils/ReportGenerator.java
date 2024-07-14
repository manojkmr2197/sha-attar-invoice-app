package com.app.sha.attar.invoice.utils;

import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportGenerator {

    public static class AggregatedData {
        int quantity;
        double soldPrice;
        double actualPrice;
        double profit;
        double totalDiscount;

        public AggregatedData(int quantity, double soldPrice, double actualPrice, double profit, double totalDiscount) {
            this.quantity = quantity;
            this.soldPrice = soldPrice;
            this.actualPrice = actualPrice;
            this.profit = profit;
            this.totalDiscount = totalDiscount;
        }
    }

    public static class AccessoryAggregatedData {
        int quantity;
        double soldPrice;

        public AccessoryAggregatedData(int quantity, double soldPrice) {
            this.quantity = quantity;
            this.soldPrice = soldPrice;
        }
    }

    private static Map<String, AggregatedData> getSalesReportData(List<BillingInvoiceModel> invoices) {
        Map<String, AggregatedData> aggregationMap = new HashMap<>();
        for (BillingInvoiceModel invoice : invoices) {
            if (invoice == null) {
                System.err.println("Null invoice encountered, skipping...");
                continue;
            }
            double totalDiscount = invoice.getDiscount();
            for (BillingItemModel item : invoice.getBillingItemModelList()) {

                if (!item.getType().equals("PRODUCT")) {
                    continue;
                }
                String productName = item.getName();
                int quantity = item.getUnits();
                double soldPrice = item.getTotalPrice();
                double actualPrice = item.getUnitPrice() * quantity;
                double profit = soldPrice - actualPrice;
                aggregationMap.putIfAbsent(productName, new AggregatedData(0, 0, 0, 0, 0));
                AggregatedData aggregatedData = aggregationMap.get(productName);
                aggregatedData.quantity += quantity;
                aggregatedData.soldPrice += soldPrice;
                aggregatedData.actualPrice += actualPrice;
                aggregatedData.profit += profit;
                aggregatedData.totalDiscount += totalDiscount;
            }
        }
        return aggregationMap;
    }

    private static Map<String, AccessoryAggregatedData> getAccessoriesReportData(List<BillingInvoiceModel> invoices) {
        Map<String, AccessoryAggregatedData> aggregationMap = new HashMap<>();
        for (BillingInvoiceModel invoice : invoices) {
            if (invoice == null) {
                System.err.println("Null invoice encountered, skipping...");
                continue;
            }
            for (BillingItemModel item : invoice.getBillingItemModelList()) {

                if (!item.getType().equals("NON_PRODUCT")) {
                    continue;
                }
                AccessoriesModel accessoriesModel = item.getAccessoriesModel();
                if (accessoriesModel == null) {
                    continue;
                }
                String productName = accessoriesModel.getName();
                double soldPrice = accessoriesModel.getPrice();
                aggregationMap.putIfAbsent(productName, new AccessoryAggregatedData(0, 0));
                AccessoryAggregatedData aggregatedData = aggregationMap.get(productName);
                aggregatedData.quantity += 1;
                aggregatedData.soldPrice += soldPrice;
            }
        }
        return aggregationMap;
    }

    public static void createExcelReport(List<BillingInvoiceModel> invoices, File file) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        prepareSalesSheet(workbook, invoices);
        prepareAccessoriesSheet(workbook, invoices);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file.getAbsolutePath());
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

    }

    private static void prepareAccessoriesSheet(Workbook workbook, List<BillingInvoiceModel> invoices) {
        Map<String, AccessoryAggregatedData> accessoryData = getAccessoriesReportData(invoices);

        Sheet sheet = workbook.createSheet("Accessories Report");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Accessory Name", "Quantity", "Sold Price"};

        for (String key : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
        }

        int rowCount = 0;
        for (Map.Entry<String, AccessoryAggregatedData> entry : accessoryData.entrySet()) {
            rowCount = rowCount + 1;
            Row row = sheet.createRow(rowCount);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(entry.getKey());
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(entry.getValue().quantity);
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(entry.getValue().soldPrice);
        }

    }

    private static void prepareSalesSheet(Workbook workbook, List<BillingInvoiceModel> invoices) {
        Map<String, AggregatedData> salesData = getSalesReportData(invoices);

        Sheet sheet = workbook.createSheet("Sales Report");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Product Name", "Quantity", "Sold Price", "Actual Price", "Profit", "Total Discount"};

        for (String key : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
        }

        int rowCount = 0;
        for (Map.Entry<String, AggregatedData> entry : salesData.entrySet()) {
            rowCount = rowCount + 1;
            Row row = sheet.createRow(rowCount);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(entry.getKey());
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(entry.getValue().quantity);
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(entry.getValue().soldPrice);
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(entry.getValue().actualPrice);
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(entry.getValue().profit);
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(entry.getValue().totalDiscount);

        }
    }
}
