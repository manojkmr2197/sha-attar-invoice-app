package com.app.sha.attar.invoice.model;

import java.time.OffsetDateTime;
import java.util.List;

public class BillingInvoiceModel {

    Long billingDate;
    Integer totalCost;
    Integer discount;
    String customerName;
    String customerPhone;
    Integer sellingCost;
    List<BillingItemModel> billingItemModelList;

    public BillingInvoiceModel(){}
    public BillingInvoiceModel(Long billingDate,Integer totalCost,Integer discount,String customerName,String customerPhone,Integer sellingCost){
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

    public Integer getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Integer totalCost) {
        this.totalCost = totalCost;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
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

    public Integer getSellingCost() {
        return sellingCost;
    }

    public void setSellingCost(Integer sellingCost) {
        this.sellingCost = sellingCost;
    }

    public List<BillingItemModel> getBillingItemModelList() {
        return billingItemModelList;
    }

    public void setBillingItemModelList(List<BillingItemModel> billingItemModelList) {
        this.billingItemModelList = billingItemModelList;
    }


}
