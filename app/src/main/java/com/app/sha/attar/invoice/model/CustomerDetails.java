package com.app.sha.attar.invoice.model;

import java.util.List;

public class CustomerDetails {

    Integer id;
    String phone_no;
    String name;
    List<CustomerHistoryModel> customerHistoryModelList;

    public CustomerDetails(){
        this.id = -1;
        this.phone_no = "";
        this.name = "";
    }
    public CustomerDetails(Integer id,String phone_no, String name)
    {
        this.id = id;
        this.phone_no = phone_no;
        this.name = name;
    }

    public CustomerDetails(String phone_no, String name, List<CustomerHistoryModel> customerHistoryModelList) {
        this.phone_no = phone_no;
        this.name = name;
        this.customerHistoryModelList = customerHistoryModelList;
    }

    public String getphone_number() {
        return phone_no;
    }

    public void setphone_number(String phone_no) {
        this.phone_no = phone_no;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CustomerHistoryModel> getCustomerHistoryModelList() {
        return customerHistoryModelList;
    }

    public void setCustomerHistoryModelList(List<CustomerHistoryModel> customerHistoryModelList) {
        this.customerHistoryModelList = customerHistoryModelList;
    }
}
