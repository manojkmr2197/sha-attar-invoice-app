package com.app.sha.attar.invoice.model;

import java.util.List;

public class CustomerDetails {

    String phone_no;
    String name;
    List<CustomerHistoryModel> customerHistoryModelList;

    public CustomerDetails(String phone_no, String name, List<CustomerHistoryModel> customerHistoryModelList) {
        this.phone_no = phone_no;
        this.name = name;
        this.customerHistoryModelList = customerHistoryModelList;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public String getName() {
        return name;
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
