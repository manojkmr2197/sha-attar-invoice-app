package com.app.sha.attar.invoice.model;

public class ProductModel {

    private Integer id;
    private String name;
    private String code;
    private Integer price;
    private String owner;
    private String status;

    public ProductModel(Integer id,String name, String code, Integer price, String owner, String status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.price = price;
        this.owner = owner;
        this.status = status;
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

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
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
