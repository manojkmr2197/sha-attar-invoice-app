package com.app.sha.attar.invoice.model;

public class GroupedItem {
    public String name;
    public Integer units;
    public int count;
    public double unitPrice;
    public double totalSellingPrice;

    public GroupedItem(String name, Integer units,double unitPrice) {
        this.name = name;
        this.units = units;
        this.count = 0;
        this.unitPrice = unitPrice;
        this.totalSellingPrice = 0;
    }
}
