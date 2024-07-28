package com.app.sha.attar.invoice.model;

public class AccessoriesModel {

    private Integer id;
    private String name;
    private Double price;
    private String documentId;
    private String owner;
    private String dealer;

    public AccessoriesModel() {
    }

    public AccessoriesModel(Integer id, String name, Double price, String documentId) {
        this.id = id;
        this.name = name;

        this.price = price;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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
