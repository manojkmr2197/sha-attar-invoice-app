package com.app.sha.attar.invoice.model;

public class AccessoriesModel {

    private Integer id;
    private String name;
    private Double sellingPrice;
    private Double actualPrice;
    private String documentId;
    private String owner;
    private String dealer;

    public AccessoriesModel() {
    }

    public AccessoriesModel(Integer id, String name, Double sellingPrice,Double actualPrice, String documentId) {
        this.id = id;
        this.name = name;
        this.sellingPrice = sellingPrice;
        this.actualPrice = actualPrice;
        this.documentId =documentId;
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

    public Double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Double getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(Double actualPrice) {
        this.actualPrice = actualPrice;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDealer() {
        return dealer;
    }

    public void setDealer(String dealer) {
        this.dealer = dealer;
    }
}
