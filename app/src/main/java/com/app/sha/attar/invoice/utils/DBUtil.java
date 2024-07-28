package com.app.sha.attar.invoice.utils;


import androidx.annotation.NonNull;

import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DBUtil {
    private static  FirebaseFirestore db;

    public DBUtil() {
        db = FirebaseFirestore.getInstance();
    }

    public static final FirebaseFirestore getInstance() {

        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }


    public void getProductDetails(FirestoreCallback<List<ProductModel>> callback) {
        // Fetch data from Firestore
        db.collection(DatabaseConstants.PRODUCTS_COLLECTION)
                .orderBy("name")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ProductModel> products = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                ProductModel product = document.toObject(ProductModel.class);
                                products.add(product);
                            }
                            callback.onCallback(products);
                        } else {
                            System.err.println("Error fetching product details: " + task.getException());
                        }
                    }
                });
    }

    public void getAllAccessories(FirestoreCallback<List<AccessoriesModel>> callback) {
        // Fetch data from Firestore
        db.collection(DatabaseConstants.ACCESSORIES_COLLECTION)
                .orderBy("name")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<AccessoriesModel> accessories = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                AccessoriesModel accessory = document.toObject(AccessoriesModel.class);
                                accessories.add(accessory);
                            }
                            callback.onCallback(accessories);
                        } else {
                            System.err.println("Error fetching Accessories details: " + task.getException());
                        }
                    }
                });
    }

    public void getBillingInvoiceDetail(FirestoreCallback<List<BillingInvoiceModel>> callback, String phoneNumber) {
        db.collection(DatabaseConstants.INVOICE_COLLECTION).whereEqualTo("customerPhone", phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<BillingInvoiceModel> saleDetails = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                BillingInvoiceModel model = document.toObject(BillingInvoiceModel.class);
                                saleDetails.add(model);
                            }
                            callback.onCallback(saleDetails);
                        } else {
                            System.err.println("Error fetching product details: " + task.getException());
                        }
                    }
                });
    }

    public void getBillingItemModelDetail(FirestoreCallback<List<BillingItemModel>> callback, Long saleId) {

        db.collection(DatabaseConstants.INVOICE_DETAILS_COLLECTION).whereEqualTo("invoiceId", saleId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<BillingItemModel> billingItemModel = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                BillingItemModel model = document.toObject(BillingItemModel.class);
                                billingItemModel.add(model);
                            }
                            callback.onCallback(billingItemModel);
                        } else {
                            System.err.println("Error fetching product details: " + task.getException());
                        }
                    }
                });
    }

    public void getBillingInvoiceDetail(FirestoreCallback<List<BillingInvoiceModel>> callback,  Long startTime,Long endTime) {
        db.collection(DatabaseConstants.INVOICE_COLLECTION).whereGreaterThanOrEqualTo("billingDate", startTime)
                .whereLessThanOrEqualTo("billingDate", endTime)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<BillingInvoiceModel> saleDetails = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                BillingInvoiceModel model = document.toObject(BillingInvoiceModel.class);
                                if(model.getBillingItemModelList() == null)
                                {
                                    System.out.println("Got null.");
                                }
                                saleDetails.add(model);
                            }
                            callback.onCallback(saleDetails);
                        } else {
                            System.err.println("Error fetching product details: " + task.getException());
                        }
                    }
                });
    }

}
