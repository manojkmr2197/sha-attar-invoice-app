package com.app.sha.attar.invoice.utils;


import androidx.annotation.NonNull;

import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.ConfigModel;
import com.app.sha.attar.invoice.model.ExpenseModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.model.SalesPersonModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DBUtil {
    private static FirebaseFirestore db;

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

    public void getBillingInvoiceDetail(FirestoreCallback<List<BillingInvoiceModel>> callback, Long startTime, Long endTime,String customerPhone) {
        db.collection(DatabaseConstants.INVOICE_COLLECTION).whereGreaterThanOrEqualTo("billingDate", startTime)
                .whereLessThanOrEqualTo("billingDate", endTime)
                .whereEqualTo("customerPhone", customerPhone)
                .orderBy("billingDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<BillingInvoiceModel> saleDetails = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                BillingInvoiceModel model = document.toObject(BillingInvoiceModel.class);
                                if (model.getBillingItemModelList() == null) {
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

    public void getBillingInvoiceDetail(FirestoreCallback<List<BillingInvoiceModel>> callback, Long startTime, Long endTime) {
        db.collection(DatabaseConstants.INVOICE_COLLECTION).whereGreaterThanOrEqualTo("billingDate", startTime)
                .whereLessThanOrEqualTo("billingDate", endTime)
                .orderBy("billingDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<BillingInvoiceModel> saleDetails = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                BillingInvoiceModel model = document.toObject(BillingInvoiceModel.class);
                                if (model.getBillingItemModelList() == null) {
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

    public void deleteRecordsBefore(FirestoreCallback<List<DocumentSnapshot>> callback, long beforeTimestamp) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query Firestore for documents with IDs (timestamps) before the given timestamp
        db.collection(DatabaseConstants.INVOICE_COLLECTION)
                .whereLessThan("billingDate", beforeTimestamp)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    callback.onCallback(querySnapshot.getDocuments());
                })
                .addOnFailureListener(e -> System.err.println("Error fetching documents: " + e.getMessage()));
    }

    public void deleteExpenseRecordsBefore(FirestoreCallback<List<DocumentSnapshot>> callback, long beforeTimestamp) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query Firestore for documents with IDs (timestamps) before the given timestamp
        db.collection(DatabaseConstants.EXPENSE_COLLECTION)
                .whereLessThan("expenseDate", beforeTimestamp)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    callback.onCallback(querySnapshot.getDocuments());
                })
                .addOnFailureListener(e -> System.err.println("Error fetching documents: " + e.getMessage()));
    }

    public void getBillingItemDetailByDocId(FirestoreCallback<BillingInvoiceModel> callback, String documentId) {

        db.collection(DatabaseConstants.INVOICE_COLLECTION).document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Map the document to the model class
                        BillingInvoiceModel model = documentSnapshot.toObject(BillingInvoiceModel.class);

                        if (model != null) {
                            // prepare invoice item details
                            callback.onCallback(model);
                        }
                    } else {
                        System.out.println("No document found with the given ID.");
                    }
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error fetching document: " + e.getMessage());
                });
    }

    public void getSalePersonDetails(FirestoreCallback<List<SalesPersonModel>> callback) {
        // Fetch data from Firestore
        db.collection(DatabaseConstants.SALES_PERSON_COLLECTION)
                .orderBy("type")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<SalesPersonModel> salesPersonList = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                SalesPersonModel product = document.toObject(SalesPersonModel.class);
                                salesPersonList.add(product);
                            }
                            callback.onCallback(salesPersonList);
                        } else {
                            System.err.println("Error fetching Sales Persons list details: " + task.getException());
                        }
                    }
                });
    }

    public void getExpenseDetail(FirestoreCallback<List<ExpenseModel>> callback, Long startTime, Long endTime) {
        db.collection(DatabaseConstants.EXPENSE_COLLECTION).whereGreaterThanOrEqualTo("expenseDate", startTime)
                .whereLessThanOrEqualTo("expenseDate", endTime)
                .orderBy("expenseDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ExpenseModel> expenseDetails = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                ExpenseModel model = document.toObject(ExpenseModel.class);
                                expenseDetails.add(model);
                            }
                            callback.onCallback(expenseDetails);
                        } else {
                            System.err.println("Error fetching expense details: " + task.getException());
                        }
                    }
                });
    }

    public void getExpenseDetail(FirestoreCallback<List<ExpenseModel>> callback, Long startTime, Long endTime,String expenseType) {
        db.collection(DatabaseConstants.EXPENSE_COLLECTION).whereGreaterThanOrEqualTo("expenseDate", startTime)
                .whereLessThanOrEqualTo("expenseDate", endTime)
                .whereEqualTo("type",expenseType)
                .orderBy("expenseDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ExpenseModel> expenseDetails = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                ExpenseModel model = document.toObject(ExpenseModel.class);
                                expenseDetails.add(model);
                            }
                            callback.onCallback(expenseDetails);
                        } else {
                            System.err.println("Error fetching expense details: " + task.getException());
                        }
                    }
                });
    }

    public void getAppConfig(FirestoreCallback<ConfigModel> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Query Firestore for documents with IDs (timestamps) before the given timestamp
        db.collection(DatabaseConstants.APP_CONFIG_COLLECTION)
                .document(DatabaseConstants.APP_CONFIG_DOCUMENT)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ConfigModel model = documentSnapshot.toObject(ConfigModel.class);
                    callback.onCallback(model);
                })
                .addOnFailureListener(e -> System.err.println("Error fetching documents: " + e.getMessage()));
    }

    public void getLoginSalesInfoDetail(FirestoreCallback<List<SalesPersonModel>> callback, String phone,String password) {
        db.collection(DatabaseConstants.SALES_PERSON_COLLECTION).whereEqualTo("phoneNo", phone)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<SalesPersonModel> saleDetails = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                SalesPersonModel model = document.toObject(SalesPersonModel.class);
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
