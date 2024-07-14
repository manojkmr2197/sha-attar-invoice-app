package com.app.sha.attar.invoice.model;

public class AccessoriesModel {

    private Integer id;
    private String name;
    private Integer price;
    private String documentId;

    public AccessoriesModel() {
    }

    public AccessoriesModel(Integer id, String name, Integer price, String documentId) {
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

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
