package com.app.sha.attar.invoice.model;

public class ProductModel {

    private String name;
    private String code;
    private String price;
    private String owner;
    private String status;

    public ProductModel(String name, String code, String price, String owner, String status) {
        this.name = name;
        this.code = code;
        this.price = price;
        this.owner = owner;
        this.status = status;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
