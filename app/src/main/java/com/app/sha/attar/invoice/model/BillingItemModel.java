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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
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
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTotalPrice() {
        return totalPrice;
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
        return sellingItemPrice;
    }

    public void setSellingItemPrice(Double sellingItemPrice) {
        this.sellingItemPrice = sellingItemPrice;
    }
}
