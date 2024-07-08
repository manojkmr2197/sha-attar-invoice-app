package com.app.sha.attar.invoice.model;

import java.time.OffsetDateTime;
import java.util.List;

public class CustomerHistoryModel {

    private Integer price;
    private Integer discount;
    private OffsetDateTime orderDate;
    private List<BillingItemModel> billingItemModelList;

    public CustomerHistoryModel(Integer price, Integer discount, OffsetDateTime orderDate,List<BillingItemModel> billingItemModelList) {
        this.price = price;
        this.discount = discount;
        this.orderDate = orderDate;
        this.billingItemModelList = billingItemModelList;
    }


    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public OffsetDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(OffsetDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public List<BillingItemModel> getBillingItemModelList() {
        return billingItemModelList;
    }

    public void setBillingItemModelList(List<BillingItemModel> billingItemModelList) {
        this.billingItemModelList = billingItemModelList;
    }
}
