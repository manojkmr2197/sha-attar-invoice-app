package com.app.sha.attar.invoice.model;

import java.util.ArrayList;
import java.util.List;

public class ClientModel {
    private String name;
    private String phone;
    private double totalSpent;
    private List<BillingInvoiceModel> invoices;

    public ClientModel(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.invoices = new ArrayList<>();
        this.totalSpent = 0.0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public List<BillingInvoiceModel> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<BillingInvoiceModel> invoices) {
        this.invoices = invoices;
    }

    public void addInvoice(BillingInvoiceModel invoice) {
        this.invoices.add(invoice);
        if (invoice.getSellingCost() != null) {
            this.totalSpent += invoice.getSellingCost();
        }
    }
}
