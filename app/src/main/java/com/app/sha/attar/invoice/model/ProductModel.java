package com.app.sha.attar.invoice.model;

public class ProductModel {

    private Integer id;
    private String name;
    private String code;
    private String price;
    private String owner;
    private String isavailable;

    public ProductModel(){}

    public ProductModel(String name, String price, String owner, String a_isavailable) {
        this.name = name;
        this.price = price;
        this.owner = owner;
        this.isavailable = a_isavailable;
    }
    public ProductModel(Integer id,String name, String code, String price, String owner, String a_isavailable) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.price = price;
        this.owner = owner;
        this.isavailable = a_isavailable;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getIsavailable() {
        return isavailable;
    }

    public void setIsavailable(String status) {
        this.isavailable = status;
    }
}
