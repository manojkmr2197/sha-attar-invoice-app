package com.app.sha.attar.invoice.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;

import java.util.UUID;

public class SingleTon {

    private static SingleTon singleTon;

    private SingleTon() {

    }

    public static final SingleTon getInstance() {

        if (singleTon == null) {
            singleTon = new SingleTon();
        }
        return singleTon;
    }


    public static final boolean isNetworkConnected(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    
    

    public static final String generateProductDocument(){
        return "PRODUCT-"+ UUID.randomUUID().toString();
    }

    public static final String generateAccessoriesDocument(){
        return "NON-PRODUCT-"+ UUID.randomUUID().toString();
    }

    public static final String generateInvoiceDetailDocument(){
        return "INVOICE-"+ UUID.randomUUID().toString();
    }

}
