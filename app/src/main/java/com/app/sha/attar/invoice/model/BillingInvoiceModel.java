package com.app.sha.attar.invoice.model;

import java.time.OffsetDateTime;
import java.util.List;

public class BillingInvoiceModel implements Cloneable{

    Long billingDate;
    Double totalCost;
    Double discount;
    String customerName;
    String customerPhone;
    String paymentMode;
    String upiPaymentStatus;
    Boolean isPrint;
    Boolean isCourier;
    Double courierAmount;
    Double cardCharges;
    String remarks;
    Double sellingCost;
    List<BillingItemModel> billingItemModelList;
    String clientName;
    String clientPhoneNo;

    // GST Fields
    Boolean isGSTApplicable;
    String customerGST;
    String placeOfSupply;
    Double cgstAmount;
    Double sgstAmount;
    Double igstAmount;
    Double taxableAmount;
    Double roundOff;
    String invoiceNumber;
    Long invoiceDate;

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
        return totalCost != null ? totalCost : 0.0;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public Double getDiscount() {
        return discount != null ? discount : 0.0;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getCustomerName() {
        return customerName != null ? customerName : "";
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone != null ? customerPhone : "";
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Double getSellingCost() {
        return sellingCost != null ? sellingCost : 0.0;
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

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public Boolean getIsPrint() {
        return isPrint;
    }

    public void setIsPrint(Boolean isPrint) {
        this.isPrint = isPrint;
    }

    public Boolean getIsCourier() {
        return isCourier != null ? isCourier : false;
    }

    public void setIsCourier(Boolean isCourier) {
        this.isCourier = isCourier;
    }

    public Double getCourierAmount() {
        return courierAmount != null ? courierAmount : 0.0;
    }

    public void setCourierAmount(Double courierAmount) {
        this.courierAmount = courierAmount;
    }

    public String getUpiPaymentStatus() {
        return upiPaymentStatus;
    }

    public void setUpiPaymentStatus(String upiPaymentStatus) {
        this.upiPaymentStatus = upiPaymentStatus;
    }

    public Double getCardCharges() {
        return cardCharges != null ? cardCharges : 0.0;
    }

    public void setCardCharges(Double cardCharges) {
        this.cardCharges = cardCharges;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhoneNo() {
        return clientPhoneNo;
    }

    public void setClientPhoneNo(String clientPhoneNo) {
        this.clientPhoneNo = clientPhoneNo;
    }

    public Boolean getIsGSTApplicable() {
        return isGSTApplicable != null ? isGSTApplicable : false;
    }

    public void setIsGSTApplicable(Boolean isGSTApplicable) {
        this.isGSTApplicable = isGSTApplicable;
    }

    public String getCustomerGST() {
        return customerGST != null ? customerGST : "";
    }

    public void setCustomerGST(String customerGST) {
        this.customerGST = customerGST;
    }

    public String getPlaceOfSupply() {
        return placeOfSupply != null ? placeOfSupply : "Inside TN";
    }

    public void setPlaceOfSupply(String placeOfSupply) {
        this.placeOfSupply = placeOfSupply;
    }

    public Double getCgstAmount() {
        return cgstAmount != null ? cgstAmount : 0.0;
    }

    public void setCgstAmount(Double cgstAmount) {
        this.cgstAmount = cgstAmount;
    }

    public Double getSgstAmount() {
        return sgstAmount != null ? sgstAmount : 0.0;
    }

    public void setSgstAmount(Double sgstAmount) {
        this.sgstAmount = sgstAmount;
    }

    public Double getIgstAmount() {
        return igstAmount != null ? igstAmount : 0.0;
    }

    public void setIgstAmount(Double igstAmount) {
        this.igstAmount = igstAmount;
    }

    public Double getTaxableAmount() {
        return taxableAmount != null ? taxableAmount : 0.0;
    }

    public void setTaxableAmount(Double taxableAmount) {
        this.taxableAmount = taxableAmount;
    }

    public Double getRoundOff() {
        return roundOff != null ? roundOff : 0.0;
    }

    public void setRoundOff(Double roundOff) {
        this.roundOff = roundOff;
    }

    public String getInvoiceNumber() {
        return invoiceNumber != null ? invoiceNumber : "";
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Long invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Double getGstTotalAmount() {
        if (isGSTApplicable != null && isGSTApplicable) {
            return (cgstAmount != null ? cgstAmount : 0.0) +
                   (sgstAmount != null ? sgstAmount : 0.0) +
                   (igstAmount != null ? igstAmount : 0.0);
        }
        return 0.0;
    }

    public Double getGrandTotal() {
        double total = getSellingCost() + getGstTotalAmount();
        if (isCourier != null && isCourier) {
            total += getCourierAmount();
        }
        if (roundOff != null) {
            total += roundOff;
        }
        return Math.round(total * 100.0) / 100.0;
    }

    @Override
    public BillingInvoiceModel clone() {
        try {
            return (BillingInvoiceModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
