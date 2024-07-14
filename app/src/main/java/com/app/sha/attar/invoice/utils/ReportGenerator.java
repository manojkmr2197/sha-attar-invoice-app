package com.app.sha.attar.invoice.utils;

import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;
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

                if(!item.getType().equals("PRODUCT"))
                {
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

                if(!item.getType().equals("NON_PRODUCT"))
                {
                    continue;
                }
                AccessoriesModel accessoriesModel = item.getAccessoriesModel();
                if(accessoriesModel == null){
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

    public static void createExcelReport(List<BillingInvoiceModel> invoices, File file) throws WriteException, IOException {
        WritableWorkbook workbook = Workbook.createWorkbook(file);
        try {
            Map<String, AggregatedData> salesData = getSalesReportData(invoices);
            Map<String, AccessoryAggregatedData> accessoryData = getAccessoriesReportData(invoices);
            int sheetIndex=0;
            System.out.println("SabeekDataReport:"+salesData.size());
            System.out.println("SabeekAcceSize:"+accessoryData.size());
            int row = 1;
            if(!salesData.isEmpty())
            {
                WritableSheet salesSheet = workbook.createSheet("Sales_Report", sheetIndex++);
                salesSheet.addCell(new Label(0, 0, "Product Name"));
                salesSheet.addCell(new Label(1, 0, "Quantity"));
                salesSheet.addCell(new Label(2, 0, "Sold Price"));
                salesSheet.addCell(new Label(3, 0, "Actual Price"));
                salesSheet.addCell(new Label(4, 0, "Profit"));
                salesSheet.addCell(new Label(5, 0, "Total Discount"));
                for (Map.Entry<String, AggregatedData> entry : salesData.entrySet()) {
                    salesSheet.addCell(new Label(0, row, entry.getKey()));
                    AggregatedData data = entry.getValue();
                    salesSheet.addCell(new Number(1, row, data.quantity));
                    salesSheet.addCell(new Number(2, row, data.soldPrice));
                    salesSheet.addCell(new Number(3, row, data.actualPrice));
                    salesSheet.addCell(new Number(4, row, data.profit));
                    salesSheet.addCell(new Number(5, row, data.totalDiscount));
                    row++;
                }
            }
            if(!accessoryData.isEmpty())
            {
                WritableSheet accessorySheet = workbook.createSheet("Accessories_Report", sheetIndex++);
                accessorySheet.addCell(new Label(0, 0, "Accessory Name"));
                accessorySheet.addCell(new Label(1, 0, "Quantity"));
                accessorySheet.addCell(new Label(2, 0, "Sold Price"));

                row = 1;
                for (Map.Entry<String, AccessoryAggregatedData> entry : accessoryData.entrySet()) {
                    accessorySheet.addCell(new Label(0, row, entry.getKey()));
                    AccessoryAggregatedData data = entry.getValue();
                    accessorySheet.addCell(new Number(1, row, data.quantity));
                    accessorySheet.addCell(new Number(2, row, data.soldPrice));
                    row++;
                }
            }
            workbook.write();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the workbook
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
