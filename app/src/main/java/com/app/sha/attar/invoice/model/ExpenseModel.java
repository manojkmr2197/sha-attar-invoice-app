package com.app.sha.attar.invoice.model;

public class ExpenseModel {

    private String id;
    private Long expenseDate;
    private String type;
    private String title;
    private double amount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(Long expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
