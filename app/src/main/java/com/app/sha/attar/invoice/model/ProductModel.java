package com.app.sha.attar.invoice.model;

import java.util.HashMap;

public class ProductModel {

    private Integer id;
    private String name;
    private String code;
    private String price;
    private String owner;
    private String status;
    private String documentId;
    private String dealer;
    private HashMap<String,Double> attarSellingPriceMap;
    private HashMap<String,Double> perfumeSellingPriceMap;
    private HashMap<String,Double> perfumeActualPriceMap;

    public ProductModel(){}

    public ProductModel(String name, String price, String owner, String a_status) {
        this.name = name;
        this.price = price;
        this.owner = owner;
        this.status = a_status;
    }
    public ProductModel(int id,String name, String code, String price, String owner, String a_status,String a_documentId) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.price = price;
        this.owner = owner;
        this.documentId = a_documentId;
        this.status = a_status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDocumentId(String a_DocumentId){this.documentId = a_DocumentId;}

    public String getDocumentId(){return documentId;}

    public String getDealer() {
        return dealer;
    }

    public void setDealer(String dealer) {
        this.dealer = dealer;
    }

    public HashMap<String, Double> getAttarSellingPriceMap() {
        return attarSellingPriceMap;
    }

    public void setAttarSellingPriceMap(HashMap<String, Double> attarSellingPriceMap) {
        this.attarSellingPriceMap = attarSellingPriceMap;
    }

    public HashMap<String, Double> getPerfumeSellingPriceMap() {
        return perfumeSellingPriceMap;
    }

    public void setPerfumeSellingPriceMap(HashMap<String, Double> perfumeSellingPriceMap) {
        this.perfumeSellingPriceMap = perfumeSellingPriceMap;
    }

    public HashMap<String, Double> getPerfumeActualPriceMap() {
        return perfumeActualPriceMap;
    }

    public void setPerfumeActualPriceMap(HashMap<String, Double> perfumeActualPriceMap) {
        this.perfumeActualPriceMap = perfumeActualPriceMap;
    }
}
