package com.app.sha.attar.invoice.utils;

import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.ReportModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportGenerator {


    public static class AggregatedData {
        public int quantity;
        public double soldPrice;
        public double actualPrice;
        public double profit;

        public AggregatedData(int quantity, double soldPrice, double actualPrice, double profit) {
            this.quantity = quantity;
            this.soldPrice = soldPrice;
            this.actualPrice = actualPrice;
            this.profit = profit;
        }
    }

    public static class AccessoryAggregatedData {
        public int quantity;
        public double soldPrice;
        public double actualPrice;
        public double profit;

        public AccessoryAggregatedData(int quantity, double soldPrice, double actualPrice, double profit) {
            this.quantity = quantity;
            this.soldPrice = soldPrice;
            this.actualPrice = actualPrice;
            this.profit = profit;
        }
    }

    public static Map<String, AggregatedData> getAggregatedSalesReportData(List<BillingInvoiceModel> invoices) {
        Map<String, AggregatedData> aggregationMap = new HashMap<>();
        for (BillingInvoiceModel invoice : invoices) {
            if (invoice == null) {
                System.err.println("Null invoice encountered, skipping...");
                continue;
            }
            for (BillingItemModel item : invoice.getBillingItemModelList()) {

                if (!item.getType().equals("PRODUCT")) {
                    continue;
                }
                String productName = item.getName();
                int quantity = item.getUnits();
                double soldPrice = item.getSellingItemPrice();
                double actualPrice = item.getUnitPrice() * quantity;
                double profit = soldPrice - actualPrice;
                aggregationMap.putIfAbsent(productName, new AggregatedData(0, 0, 0, 0));
                AggregatedData aggregatedData = aggregationMap.get(productName);
                aggregatedData.quantity += quantity;
                aggregatedData.soldPrice += soldPrice;
                aggregatedData.actualPrice += actualPrice;
                aggregatedData.profit += profit;
            }
        }
        return aggregationMap;
    }

    public static Map<String, AccessoryAggregatedData> getAggregatedAccessoriesReportData(List<BillingInvoiceModel> invoices) {
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

                String productName = item.getName();
                double soldPrice = item.getSellingItemPrice();
                double actualPrice = item.getTotalPrice();
                double profit = soldPrice - actualPrice;
                aggregationMap.putIfAbsent(productName, new AccessoryAggregatedData(0, 0, 0, 0));
                AccessoryAggregatedData aggregatedData = aggregationMap.get(productName);
                aggregatedData.quantity += 1;
                aggregatedData.soldPrice += soldPrice;
                aggregatedData.actualPrice += actualPrice;
                aggregatedData.profit += profit;

            }
        }
        return aggregationMap;
    }


    private ReportModel getReportModel(BillingItemModel item, BillingInvoiceModel invoice) {
        double soldPrice = item.getSellingItemPrice();
        double actualPrice = item.getTotalPrice();
        double profit = soldPrice - actualPrice;
        ReportModel report = new ReportModel();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);
            OffsetDateTime offsetDateTime = Instant.ofEpochSecond(invoice.getBillingDate()).atOffset(istOffset);
            report.setDate(offsetDateTime.format(formatter));
        }
        report.setName(item.getName());
        report.setActualPrice(actualPrice);
        report.setQuantity((item.getUnits() != null) ? item.getUnits() : 1);
        report.setProfit(profit);
        report.setSoldPrice(item.getSellingItemPrice());
        if (!StringUtils.isBlank(invoice.getRemarks()))
            report.setCustomerInfo(invoice.getRemarks() + "(" + invoice.getCustomerPhone() + ")");
        return report;
    }

    public List<ReportModel> getSalesReportData(List<BillingInvoiceModel> invoices) {
        List<ReportModel> reportProductList = new ArrayList<>();
        for (BillingInvoiceModel invoice : invoices) {
            for (BillingItemModel item : invoice.getBillingItemModelList()) {
                if (!item.getType().equals("PRODUCT")) {
                    continue;
                }
                reportProductList.add(getReportModel(item, invoice));
            }
        }

        double totalActual = 0.0;
        double totalSold = 0.0;
        double totalProfit = 0.0;
        int totalQuantity = 0;
        for (ReportModel data : reportProductList) {
            totalActual += data.getActualPrice();
            totalSold += data.getSoldPrice();
            totalProfit += data.getProfit();
            totalQuantity += data.getQuantity();
        }
        ReportModel report = new ReportModel();
        report.setDate("");
        report.setName("Total");
        report.setActualPrice(totalActual);
        report.setQuantity(totalQuantity);
        report.setProfit(totalProfit);
        report.setSoldPrice(totalSold);
        reportProductList.add(report);

        return reportProductList;
    }

    public List<ReportModel> getAccessoriesReportData(List<BillingInvoiceModel> invoices) {
        List<ReportModel> reportAccessoriesList = new ArrayList<>();
        for (BillingInvoiceModel invoice : invoices) {
            for (BillingItemModel item : invoice.getBillingItemModelList()) {
                if (!item.getType().equals("NON_PRODUCT")) {
                    continue;
                }
                reportAccessoriesList.add(getReportModel(item, invoice));
            }
        }

        double totalActual = 0.0;
        double totalSold = 0.0;
        double totalProfit = 0.0;
        int totalQuantity = 0;
        for (ReportModel data : reportAccessoriesList) {
            totalActual += data.getActualPrice();
            totalSold += data.getSoldPrice();
            totalProfit += data.getProfit();
            totalQuantity += data.getQuantity();
        }
        ReportModel report = new ReportModel();
        report.setDate("");
        report.setName("Total");
        report.setActualPrice(totalActual);
        report.setQuantity(totalQuantity);
        report.setProfit(totalProfit);
        report.setSoldPrice(totalSold);
        reportAccessoriesList.add(report);

        return reportAccessoriesList;
    }

    public void createExcelReport(List<BillingInvoiceModel> invoices, File file, OffsetDateTime startOfDay, OffsetDateTime endOfDay) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        prepareSalesSheet(workbook, invoices);
        prepareAccessoriesSheet(workbook, invoices);
        prepareConsolidatedSaleReport(workbook, invoices, startOfDay, endOfDay);
        prepareConsolidatedAccessoryReport(workbook, invoices, startOfDay, endOfDay);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file.getAbsolutePath());
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

    }

    private void prepareConsolidatedAccessoryReport(Workbook workbook, List<BillingInvoiceModel> invoices, OffsetDateTime startOfDay, OffsetDateTime endOfDay) {

        Map<String, AccessoryAggregatedData> salesData = getAggregatedAccessoriesReportData(invoices);

        Sheet sheet = workbook.createSheet("Consolidated Accessory Report");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Row dateRow = sheet.createRow(0);
        Cell start_Text = dateRow.createCell(0);
        start_Text.setCellValue("Starting Date");
        Cell start_cell = dateRow.createCell(1);
        start_cell.setCellValue(startOfDay.format(formatter));

        Row dateRow1 = sheet.createRow(1);
        Cell end_Text = dateRow1.createCell(0);
        end_Text.setCellValue("End Date");
        Cell end_cell = dateRow1.createCell(1);
        end_cell.setCellValue(endOfDay.format(formatter));

        Row headerRow = sheet.createRow(2);
        int cellIndex = 0;

        String[] headers = {"Product Name", "Quantity", "Sold Price", "Actual Price", "Profit"};

        for (String key : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
        }

        List<Map.Entry<String, AccessoryAggregatedData>> itemsList = new ArrayList<>(salesData.entrySet());

        itemsList.sort((entry1, entry2) -> Double.compare(entry2.getValue().profit, entry1.getValue().profit));

        int rowCount = 2;
        for (Map.Entry<String, AccessoryAggregatedData> entry : itemsList) {
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

        }

    }

    private void prepareConsolidatedSaleReport(Workbook workbook, List<BillingInvoiceModel> invoices, OffsetDateTime startOfDay, OffsetDateTime endOfDay) {

        Map<String, AggregatedData> salesData = getAggregatedSalesReportData(invoices);

        Sheet sheet = workbook.createSheet("Consolidated Sale Report");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Row dateRow = sheet.createRow(0);
        Cell start_Text = dateRow.createCell(0);
        start_Text.setCellValue("Starting Date");
        Cell start_cell = dateRow.createCell(1);
        start_cell.setCellValue(startOfDay.format(formatter));

        Row dateRow1 = sheet.createRow(1);
        Cell end_Text = dateRow1.createCell(0);
        end_Text.setCellValue("End Date");
        Cell end_cell = dateRow1.createCell(1);
        end_cell.setCellValue(endOfDay.format(formatter));

        Row headerRow = sheet.createRow(2);
        int cellIndex = 0;

        String[] headers = {"Product Name", "Quantity", "Sold Price", "Actual Price", "Profit"};

        for (String key : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
        }

        List<Map.Entry<String, AggregatedData>> itemsList = new ArrayList<>(salesData.entrySet());

        itemsList.sort((entry1, entry2) -> Double.compare(entry2.getValue().profit, entry1.getValue().profit));

        int rowCount = 2;
        for (Map.Entry<String, AggregatedData> entry : itemsList) {
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

        }

    }

    private void prepareAccessoriesSheet(Workbook workbook, List<BillingInvoiceModel> invoices) {


        List<ReportModel> accessoryData = getAccessoriesReportData(invoices);

        Sheet sheet = workbook.createSheet("Accessories Report");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Date", "Accessory Name", "Quantity", "Sold Price", "Actual Price", "Profit", "Customer Remarks"};

        for (String key : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
        }

        int rowCount = 0;
        for (ReportModel entry : accessoryData) {
            rowCount = rowCount + 1;
            Row row = sheet.createRow(rowCount);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(entry.getDate());
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(entry.getName());
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(entry.getQuantity());
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(entry.getSoldPrice());
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(entry.getActualPrice());
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(entry.getProfit());
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(entry.getCustomerInfo());
        }

    }

    private void prepareSalesSheet(Workbook workbook, List<BillingInvoiceModel> invoices) {
        List<ReportModel> reportData = getSalesReportData(invoices);

        Sheet sheet = workbook.createSheet("Sales Report");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Date", "Product Name", "Quantity", "Sold Price", "Actual Price", "Profit", "Customer Remarks"};

        for (String key : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
        }

        int rowCount = 0;
        for (ReportModel entry : reportData) {
            rowCount = rowCount + 1;
            Row row = sheet.createRow(rowCount);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(entry.getDate());
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(entry.getName());
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(entry.getQuantity());
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(entry.getSoldPrice());
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(entry.getActualPrice());
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(entry.getProfit());
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(entry.getCustomerInfo());

        }
    }
}
