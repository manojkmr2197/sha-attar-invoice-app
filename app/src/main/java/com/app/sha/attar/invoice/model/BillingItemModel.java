package com.app.sha.attar.invoice.model;

public class BillingItemModel {

    String type;
    String name;
    String code;
    Integer units;
    Double unitPrice;
    Double totalPrice;
    Double sellingItemPrice;
    ProductModel productModel;
    AccessoriesModel accessoriesModel;
    Long invoiceId;
    String productCategory;
    Integer pieces;

    // GST Fields
    String hsnCode;
    Double gstPercentage;
    Double gstAmount;
    Double taxableValue;
    Double discount;

    public BillingItemModel() {
    }

    public BillingItemModel(String type, String name, String code, Integer units, Double unitPrice, Double totalPrice) {
        this.type = type;
        this.name = name;
        this.code = code;
        this.units = units;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    public Double getUnitPrice() {
        return unitPrice != null ? unitPrice : 0.0;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTotalPrice() {
        return totalPrice != null ? totalPrice : 0.0;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public ProductModel getProductModel() {
        return productModel;
    }

    public void setProductModel(ProductModel productModel) {
        this.productModel = productModel;
    }

    public AccessoriesModel getAccessoriesModel() {
        return accessoriesModel;
    }

    public void setAccessoriesModel(AccessoriesModel accessoriesModel) {
        this.accessoriesModel = accessoriesModel;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Double getSellingItemPrice() {
        return sellingItemPrice != null ? sellingItemPrice : 0.0;
    }

    public void setSellingItemPrice(Double sellingItemPrice) {
        this.sellingItemPrice = sellingItemPrice;
    }

    public Integer getPieces() {
        return pieces != null ? pieces : 1;
    }

    public void setPieces(Integer pieces) {
        this.pieces = pieces;
    }

    public String getHsnCode() {
        return hsnCode != null ? hsnCode : "";
    }

    public void setHsnCode(String hsnCode) {
        this.hsnCode = hsnCode;
    }

    public Double getGstPercentage() {
        return gstPercentage != null ? gstPercentage : 0.0;
    }

    public void setGstPercentage(Double gstPercentage) {
        this.gstPercentage = gstPercentage;
    }

    public Double getGstAmount() {
        return gstAmount != null ? gstAmount : 0.0;
    }

    public void setGstAmount(Double gstAmount) {
        this.gstAmount = gstAmount;
    }

    public Double getTaxableValue() {
        return taxableValue != null ? taxableValue : 0.0;
    }

    public void setTaxableValue(Double taxableValue) {
        this.taxableValue = taxableValue;
    }

    public Double getDiscount() {
        return discount != null ? discount : 0.0;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }
}
