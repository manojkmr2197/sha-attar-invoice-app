package com.app.sha.attar.invoice.utils;

import android.content.Context;
import android.net.Uri;

import com.app.sha.attar.invoice.activity.ProductActivity;
import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.ConfigModel;
import com.app.sha.attar.invoice.model.ExpenseModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.model.ReportModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportGenerator {




    public static class AggregatedData {
        public int quantity;
        public double soldPrice;
        public double actualPrice;
        public double profit;
        public String owner;

        public AggregatedData(int quantity, double soldPrice, double actualPrice, double profit, String owner) {
            this.quantity = quantity;
            this.soldPrice = soldPrice;
            this.actualPrice = actualPrice;
            this.profit = profit;
            this.owner = owner;
        }
    }

    public static class AccessoryAggregatedData {
        public int quantity;
        public double soldPrice;
        public double actualPrice;
        public double profit;
        public String owner;

        public AccessoryAggregatedData(int quantity, double soldPrice, double actualPrice, double profit, String owner) {
            this.quantity = quantity;
            this.soldPrice = soldPrice;
            this.actualPrice = actualPrice;
            this.profit = profit;
            this.owner = owner;
        }
    }

    public static Map<String, AggregatedData> getAggregatedAttarSalesReportData(List<BillingInvoiceModel> invoices) {
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

                if (!item.getProductCategory().equals("ATTAR")) {
                    continue;
                }
                String productName = item.getName();
                int quantity = item.getUnits();
                double soldPrice = item.getSellingItemPrice() - (item.getSellingItemPrice() * (invoice.getDiscount() / 100));
                //double actualPrice = item.getUnitPrice() * quantity;
                double actualPrice = item.getTotalPrice();
                double profit = soldPrice - actualPrice;
                aggregationMap.putIfAbsent(productName, new AggregatedData(0, 0, 0, 0, ""));
                AggregatedData aggregatedData = aggregationMap.get(productName);
                aggregatedData.quantity += quantity;
                aggregatedData.soldPrice += soldPrice;
                aggregatedData.actualPrice += actualPrice;
                aggregatedData.profit += profit;
                aggregatedData.owner = item.getProductModel().getOwner();
            }
        }
        return aggregationMap;
    }

    public static Map<String, AggregatedData> getAggregatedSpraySalesReportData(List<BillingInvoiceModel> invoices) {
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
                if (!item.getProductCategory().equals("SPRAY")) {
                    continue;
                }
                String productName = item.getName();
                int quantity = item.getUnits();
                double soldPrice = item.getSellingItemPrice() - (item.getSellingItemPrice() * (invoice.getDiscount() / 100));
                //double actualPrice = item.getUnitPrice() * quantity;
                double actualPrice = item.getTotalPrice();
                double profit = soldPrice - actualPrice;
                aggregationMap.putIfAbsent(productName, new AggregatedData(0, 0, 0, 0, ""));
                AggregatedData aggregatedData = aggregationMap.get(productName);
                aggregatedData.quantity += quantity;
                aggregatedData.soldPrice += soldPrice;
                aggregatedData.actualPrice += actualPrice;
                aggregatedData.profit += profit;
                aggregatedData.owner = item.getProductModel().getOwner();
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
                double soldPrice = item.getSellingItemPrice() - (item.getSellingItemPrice() * (invoice.getDiscount() / 100));
                double actualPrice = item.getTotalPrice();
                double profit = soldPrice - actualPrice;
                aggregationMap.putIfAbsent(productName, new AccessoryAggregatedData(0, 0, 0, 0, ""));
                AccessoryAggregatedData aggregatedData = aggregationMap.get(productName);
                aggregatedData.quantity += 1;
                aggregatedData.soldPrice += soldPrice;
                aggregatedData.actualPrice += actualPrice;
                aggregatedData.profit += profit;
                aggregatedData.owner = item.getAccessoriesModel().getOwner();

            }
        }
        return aggregationMap;
    }


    private ReportModel getReportModel(BillingItemModel item, BillingInvoiceModel invoice) {
        double soldPrice = item.getSellingItemPrice() - (item.getSellingItemPrice() * (invoice.getDiscount() / 100));
        double actualPrice = item.getTotalPrice();
        double profit = soldPrice - actualPrice;
        ReportModel report = new ReportModel();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);
            OffsetDateTime offsetDateTime = Instant.ofEpochSecond(invoice.getBillingDate()).atOffset(istOffset);
            report.setDate(offsetDateTime.format(formatter));
        }
        if (item.getType().equals("PRODUCT")) {
            report.setOwner(item.getProductModel().getOwner());
        } else if (item.getType().equals("NON_PRODUCT")) {
            report.setOwner(item.getAccessoriesModel().getOwner());
        }
        if(invoice.getIsCourier() != null && invoice.getIsCourier()){
            report.setSalesType("COURIER");
        }else{
            report.setSalesType("STORE_SALES");
        }

        report.setName(item.getName());
        report.setActualPrice(actualPrice);
        report.setQuantity((item.getUnits() != null) ? item.getUnits() : 1);
        report.setProfit(profit);
        report.setSoldPrice(item.getSellingItemPrice() - (item.getSellingItemPrice() * (invoice.getDiscount() / 100)));
        if (!StringUtils.isBlank(invoice.getCustomerName()))
            report.setCustomerInfo(invoice.getCustomerName() + "(" + invoice.getCustomerPhone() + ")");
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

        Collections.sort(invoices, Comparator.comparingLong(BillingInvoiceModel::getBillingDate));

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

        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
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

        String[] headers = {"Product Name", "Owner", "Quantity", "Sold Price", "Actual Price", "Profit"};

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
            cell0.setCellStyle(wrapStyle);
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(entry.getValue().owner);
            cell1.setCellStyle(wrapStyle);
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(entry.getValue().quantity);
            cell2.setCellStyle(wrapStyle);
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(entry.getValue().soldPrice);
            cell3.setCellStyle(wrapStyle);
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(entry.getValue().actualPrice);
            cell4.setCellStyle(wrapStyle);
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(entry.getValue().profit);
            cell5.setCellStyle(wrapStyle);

        }

    }

    private void prepareConsolidatedSaleReport(Workbook workbook, List<BillingInvoiceModel> invoices, OffsetDateTime startOfDay, OffsetDateTime endOfDay) {
        Map<String, AggregatedData> salesAttarData = getAggregatedAttarSalesReportData(invoices);

        prepareProductReport(workbook, startOfDay, endOfDay, salesAttarData,"Attar");

        Map<String, AggregatedData> salesData = getAggregatedSpraySalesReportData(invoices);

        prepareProductReport(workbook, startOfDay, endOfDay, salesData,"Spray");
    }

    private void prepareProductReport(Workbook workbook, OffsetDateTime startOfDay, OffsetDateTime endOfDay, Map<String, AggregatedData> salesData,String type) {
        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);

        Sheet sheet = workbook.createSheet("Consolidated "+type+" Report");

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

        String[] headers = {"Product Name", "Owner", "Quantity", "Sold Price", "Actual Price", "Profit"};

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
            cell0.setCellStyle(wrapStyle);
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(entry.getValue().owner);
            cell1.setCellStyle(wrapStyle);
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(entry.getValue().quantity);
            cell2.setCellStyle(wrapStyle);
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(entry.getValue().soldPrice);
            cell3.setCellStyle(wrapStyle);
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(entry.getValue().actualPrice);
            cell4.setCellStyle(wrapStyle);
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(entry.getValue().profit);
            cell5.setCellStyle(wrapStyle);

        }
    }

    private void prepareAccessoriesSheet(Workbook workbook, List<BillingInvoiceModel> invoices) {

        List<ReportModel> accessoryData = getAccessoriesReportData(invoices);

        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        Sheet sheet = workbook.createSheet("Accessories Report");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Date", "Accessory Name", "Owner", "Quantity", "Sold Price", "Actual Price", "Profit", "Sales Info","Sales Type"};

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
            cell0.setCellStyle(wrapStyle);
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(entry.getName());
            cell1.setCellStyle(wrapStyle);
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(entry.getOwner());
            cell2.setCellStyle(wrapStyle);
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(entry.getQuantity());
            cell3.setCellStyle(wrapStyle);
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(entry.getSoldPrice());
            cell4.setCellStyle(wrapStyle);
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(entry.getActualPrice());
            cell5.setCellStyle(wrapStyle);
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(entry.getProfit());
            cell6.setCellStyle(wrapStyle);
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(entry.getCustomerInfo());
            cell7.setCellStyle(wrapStyle);
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(entry.getSalesType());
            cell8.setCellStyle(wrapStyle);
        }


    }

    private void prepareSalesSheet(Workbook workbook, List<BillingInvoiceModel> invoices) {
        List<ReportModel> reportData = getSalesReportData(invoices);

        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        Sheet sheet = workbook.createSheet("Sales Report");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Date", "Product Name", "Owner", "Quantity", "Sold Price", "Actual Price", "Profit", "Sales Info","Sales Type"};

        for (String key : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
            cell.setCellStyle(wrapStyle);
        }

        int rowCount = 0;
        for (ReportModel entry : reportData) {
            rowCount = rowCount + 1;
            Row row = sheet.createRow(rowCount);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(entry.getDate());
            cell0.setCellStyle(wrapStyle);
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(entry.getName());
            cell1.setCellStyle(wrapStyle);
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(entry.getOwner());
            cell2.setCellStyle(wrapStyle);
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(entry.getQuantity());
            cell3.setCellStyle(wrapStyle);
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(entry.getSoldPrice());
            cell4.setCellStyle(wrapStyle);
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(entry.getActualPrice());
            cell5.setCellStyle(wrapStyle);
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(entry.getProfit());
            cell6.setCellStyle(wrapStyle);
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(entry.getCustomerInfo());
            cell7.setCellStyle(wrapStyle);
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(entry.getSalesType());
            cell8.setCellStyle(wrapStyle);

        }

    }


    public void createProductExcelReport(List<ProductModel> productModelList, File file) throws Exception {

        Workbook workbook = new XSSFWorkbook();

        prepareProductSheet(workbook, productModelList);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file.getAbsolutePath());
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

    }

    private void prepareProductSheet(Workbook workbook, List<ProductModel> productList) {

        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        Sheet sheet = workbook.createSheet("Product Details");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Name", "Dealer", "Price", "Owner", "Status", "Attar 6 ML", "Perfume 10 ML", "Perfume 30 ML", "Perfume 50 ML", "Perfume 100 ML"};

        for (String key : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
            cell.setCellStyle(wrapStyle);
        }

        int rowCount = 0;
        for (ProductModel entry : productList) {
            rowCount = rowCount + 1;
            Row row = sheet.createRow(rowCount);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(entry.getName());
            cell0.setCellStyle(wrapStyle);
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(SingleTon.getDealerName(entry.getDealer()));
            cell1.setCellStyle(wrapStyle);
            Cell cell2 = row.createCell(2);
            cell2.setCellValue("Rs. " + entry.getPrice());
            cell2.setCellStyle(wrapStyle);
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(entry.getOwner());
            cell3.setCellStyle(wrapStyle);
            Cell cell4 = row.createCell(4);
            cell4.setCellValue((entry.getStatus().equalsIgnoreCase("Y") ? "Available" : "Out-Of-Stock"));
            cell4.setCellStyle(wrapStyle);
            Cell cell5 = row.createCell(5);
            cell5.setCellValue("Rs. " + entry.getAttarSellingPriceMap().get(AppConstants.ML_6));
            cell5.setCellStyle(wrapStyle);

            Cell cell6 = row.createCell(6);
            cell6.setCellValue("Rs. " + entry.getPerfumeSellingPriceMap().get(AppConstants.ML_10));
            cell6.setCellStyle(wrapStyle);

            Cell cell7 = row.createCell(7);
            cell7.setCellValue("Rs. " + entry.getPerfumeSellingPriceMap().get(AppConstants.ML_30));
            cell7.setCellStyle(wrapStyle);

            Cell cell8 = row.createCell(8);
            cell8.setCellValue("Rs. " + entry.getPerfumeSellingPriceMap().get(AppConstants.ML_50));
            cell8.setCellStyle(wrapStyle);

            Cell cell9 = row.createCell(9);
            cell9.setCellValue("Rs. " + entry.getPerfumeSellingPriceMap().get(AppConstants.ML_100));
            cell9.setCellStyle(wrapStyle);
        }

    }

    public void createAccessoriesExcelReport(List<AccessoriesModel> accessoriesModelList, File file) throws Exception {

        Workbook workbook = new XSSFWorkbook();

        prepareAccessoriesListSheet(workbook, accessoriesModelList);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file.getAbsolutePath());
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }

    private void prepareAccessoriesListSheet(Workbook workbook, List<AccessoriesModel> accessoriesModelList) {

        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        Sheet sheet = workbook.createSheet("Accessories Details");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Name", "Selling Price", "Owner", "Dealer"," Actual Price",};

        for (String key : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
            cell.setCellStyle(wrapStyle);
        }

        int rowCount = 0;
        for (AccessoriesModel entry : accessoriesModelList) {
            rowCount = rowCount + 1;
            Row row = sheet.createRow(rowCount);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(entry.getName());
            cell0.setCellStyle(wrapStyle);
            Cell cell1 = row.createCell(1);
            cell1.setCellValue("Rs. " + entry.getSellingPrice());
            cell1.setCellStyle(wrapStyle);
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(entry.getOwner());
            cell2.setCellStyle(wrapStyle);
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(SingleTon.getDealerName(entry.getDealer()));
            cell3.setCellStyle(wrapStyle);
            Cell cell4 = row.createCell(4);
            cell4.setCellValue("Rs. " + entry.getActualPrice());
            cell4.setCellStyle(wrapStyle);
        }

    }

    public List<ProductModel> readExcelFile(Uri fileUri, Context context) throws Exception {
        List<ProductModel> productModelList = new ArrayList<>();

        SharedPrefHelper helper = new SharedPrefHelper(context);
        ConfigModel configModel = helper.getPerfumeActualMix();
        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {

            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // First sheet
            DataFormatter formatter = new DataFormatter();
            boolean isHeader = true;
            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                Cell nameCell = row.getCell(0);
                Cell dealerCell = row.getCell(1);
                Cell priceCell = row.getCell(2);
                Cell ownerCell = row.getCell(3);
                Cell statusCell = row.getCell(4);
                Cell attar6mlCell = row.getCell(5);
                Cell perfume10mlCell = row.getCell(6);
                Cell perfume30mlCell = row.getCell(7);
                Cell perfume50mlCell = row.getCell(8);
                Cell perfume100mlCell = row.getCell(9);

                if (nameCell == null || dealerCell == null || priceCell == null || ownerCell == null ||
                        statusCell == null || attar6mlCell == null || perfume10mlCell == null || perfume30mlCell == null ||
                        perfume50mlCell == null || perfume100mlCell == null)
                    continue;
                ProductModel productModel = new ProductModel();
                productModel.setName(formatter.formatCellValue(nameCell));
                productModel.setDealer(formatter.formatCellValue(dealerCell));
                productModel.setPrice(formatter.formatCellValue(priceCell).replace("Rs.","").trim());
                productModel.setOwner(formatter.formatCellValue(ownerCell));
                productModel.setStatus("Available".equalsIgnoreCase(formatter.formatCellValue(statusCell))?"Y":"N");

                double attar1ml = Double.parseDouble(formatter.formatCellValue(attar6mlCell).replace("Rs.","").trim())/6;
                productModel.setAttarSellingPriceMap(new HashMap<>());
                productModel.getAttarSellingPriceMap().put(AppConstants.ML_3, attar1ml * 3);
                productModel.getAttarSellingPriceMap().put(AppConstants.ML_6, attar1ml * 6);
                productModel.getAttarSellingPriceMap().put(AppConstants.ML_12, attar1ml * 12);
                productModel.getAttarSellingPriceMap().put(AppConstants.ML_24, attar1ml * 24);

                productModel.setPerfumeSellingPriceMap(new HashMap<>());
                productModel.getPerfumeSellingPriceMap().put(AppConstants.ML_10, Double.parseDouble(formatter.formatCellValue(perfume10mlCell).replace("Rs.","").trim()));
                productModel.getPerfumeSellingPriceMap().put(AppConstants.ML_30, Double.parseDouble(formatter.formatCellValue(perfume30mlCell).replace("Rs.","").trim()));
                productModel.getPerfumeSellingPriceMap().put(AppConstants.ML_50, Double.parseDouble(formatter.formatCellValue(perfume50mlCell).replace("Rs.","").trim()));
                productModel.getPerfumeSellingPriceMap().put(AppConstants.ML_100, Double.parseDouble(formatter.formatCellValue(perfume100mlCell).replace("Rs.","").trim()));

                productModel.setDocumentId(SingleTon.generateProductDocument());

                productModelList.add(productModel);
            }

            workbook.close();

            // Do something with excelData

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return productModelList;

    }

    public void createExpenseExcelReport(List<ExpenseModel> expenseList, File file) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        prepareExpenseSheet(workbook, expenseList);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file.getAbsolutePath());
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }

    private void prepareExpenseSheet(Workbook workbook, List<ExpenseModel> expenseList) {

        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        Sheet sheet = workbook.createSheet("Expense Details");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Expense Date", "Category", "Title", "Price"};

        for (String key : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
            cell.setCellStyle(wrapStyle);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);
        int rowCount = 0;
        for (ExpenseModel entry : expenseList) {
            rowCount = rowCount + 1;
            Row row = sheet.createRow(rowCount);
            OffsetDateTime offsetDateTime = Instant.ofEpochSecond(entry.getExpenseDate()).atOffset(istOffset);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(offsetDateTime.format(formatter));
            cell0.setCellStyle(wrapStyle);
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(entry.getType());
            cell1.setCellStyle(wrapStyle);
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(entry.getTitle());
            cell2.setCellStyle(wrapStyle);
            Cell cell3 = row.createCell(3);
            cell3.setCellValue("Rs. " +String.format("%.1f", entry.getAmount()));
            cell3.setCellStyle(wrapStyle);
        }

    }

}
