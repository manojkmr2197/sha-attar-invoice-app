package com.app.sha.attar.invoice.model;

public class BillingItemModel {

    String type;
    String name;
    String code;
    Integer units;
    Integer unitPrice;
    Integer totalPrice;
    ProductModel productModel;
    AccessoriesModel accessoriesModel;
    String saleId;

    public BillingItemModel() {
    }

    public BillingItemModel(String type, String name, String code, Integer units, Integer unitPrice, Integer totalPrice) {
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

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Integer unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
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

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }
}
