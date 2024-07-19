package com.app.sha.attar.invoice.model;

import java.time.OffsetDateTime;
import java.util.List;

public class BillingInvoiceModel {

    Long billingDate;
    Double totalCost;
    Double discount;
    String customerName;
    String customerPhone;
    String remarks;
    Double sellingCost;
    List<BillingItemModel> billingItemModelList;

    public BillingInvoiceModel(){}
    public BillingInvoiceModel(Long billingDate,Double totalCost,Double discount,String customerName,String customerPhone,Double sellingCost){
        this.billingDate=billingDate;
        this.totalCost=totalCost;
        this.discount=discount;
        this.customerName=customerName;
        this.customerPhone=customerPhone;
        this.sellingCost=sellingCost;

    }
    public Long getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(Long billingDate) {
        this.billingDate = billingDate;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Double getSellingCost() {
        return sellingCost;
    }

    public void setSellingCost(Double sellingCost) {
        this.sellingCost = sellingCost;
    }

    public List<BillingItemModel> getBillingItemModelList() {
        return billingItemModelList;
    }

    public void setBillingItemModelList(List<BillingItemModel> billingItemModelList) {
        this.billingItemModelList = billingItemModelList;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
