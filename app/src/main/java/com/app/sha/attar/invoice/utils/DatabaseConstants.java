package com.app.sha.attar.invoice.utils;

public class DatabaseConstants {
    private DatabaseConstants() {
        // Prevent instantiation
    }
    // Collection names
    public static final String PRODUCTS_COLLECTION = "product_details";
    //public static final String USERS_COLLECTION = "users";
   // public static final String ORDERS_COLLECTION = "orders";


    // Product document fields
    public static final String PRODUCT_ID = "code";
    public static final String PRODUCT_NAME = "name";
    public static final String PRODUCT_OWNER = "owner";
    public static final String PRODUCT_PRICE = "price";
    public static final String PRODUCT_STATUS = "status";
    /*
    // User document fields
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";
    public static final String USER_PHONE = "phone";

    // Order document fields
    public static final String ORDER_ID = "orderId";
    public static final String ORDER_DATE = "orderDate";
    public static final String ORDER_AMOUNT = "amount";
    */

}
