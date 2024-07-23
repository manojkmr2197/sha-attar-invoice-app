package com.app.sha.attar.invoice.utils;

import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.ReportModel;

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
import java.util.List;

public class ReportGenerator {

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
        report.setQuantity((item.getUnits()!=null)?item.getUnits():1);
        report.setProfit(profit);
        report.setSoldPrice(item.getSellingItemPrice());
        return report;
    }

    public List<ReportModel> getSalesReportData(List<BillingInvoiceModel> invoices) {
        List<ReportModel> reportProductList = new ArrayList<>();
        for (BillingInvoiceModel invoice : invoices){
            for (BillingItemModel item : invoice.getBillingItemModelList()) {
                if (!item.getType().equals("PRODUCT")) {
                    continue;
                }
                reportProductList.add(getReportModel(item,invoice));
            }
        }

        double totalActual = 0.0;
        double totalSold = 0.0;
        double totalProfit = 0.0;
        int totalQuantity =0;
        for(ReportModel data : reportProductList){
            totalActual+=data.getActualPrice();
            totalSold+=data.getSoldPrice();
            totalProfit+=data.getProfit();
            totalQuantity+=data.getQuantity();
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
        for (BillingInvoiceModel invoice : invoices){
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
        int totalQuantity =0;
        for(ReportModel data : reportAccessoriesList){
            totalActual+=data.getActualPrice();
            totalSold+=data.getSoldPrice();
            totalProfit+=data.getProfit();
            totalQuantity+=data.getQuantity();
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

    public void createExcelReport(List<BillingInvoiceModel> invoices, File file) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        prepareSalesSheet(workbook, invoices);
        prepareAccessoriesSheet(workbook, invoices);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file.getAbsolutePath());
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

    }

    private void prepareAccessoriesSheet(Workbook workbook, List<BillingInvoiceModel> invoices) {


        List<ReportModel> accessoryData = getAccessoriesReportData(invoices);

        Sheet sheet = workbook.createSheet("Accessories Report");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Date","Accessory Name", "Quantity", "Sold Price", "Actual Price", "Profit"};

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
        }

    }

    private void prepareSalesSheet(Workbook workbook, List<BillingInvoiceModel> invoices) {
        List<ReportModel> reportData = getSalesReportData(invoices);

        Sheet sheet = workbook.createSheet("Sales Report");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Date","Product Name", "Quantity", "Sold Price", "Actual Price", "Profit"};

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

        }
    }
}
