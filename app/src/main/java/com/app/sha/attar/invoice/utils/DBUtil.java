package com.app.sha.attar.invoice.utils;


import androidx.annotation.NonNull;

import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.ConfigModel;
import com.app.sha.attar.invoice.model.ExpenseModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.model.SalesPersonModel;
import com.app.sha.attar.invoice.model.TaxModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
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

    public void getClientInvoiceByPhone(String phoneNumber, FirestoreCallback<List<BillingInvoiceModel>> callback) {
        db.collection(DatabaseConstants.INVOICE_COLLECTION)
                .whereEqualTo("clientPhoneNo", phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            List<BillingInvoiceModel> saleDetails = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                BillingInvoiceModel model = document.toObject(BillingInvoiceModel.class);
                                saleDetails.add(model);
                            }
                            callback.onCallback(saleDetails);
                        } else {
                            db.collection(DatabaseConstants.INVOICE_COLLECTION)
                                    .whereEqualTo("customerPhone", phoneNumber)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                                            List<BillingInvoiceModel> saleDetails = new ArrayList<>();
                                            if (task2.isSuccessful()) {
                                                for (DocumentSnapshot document : task2.getResult()) {
                                                    BillingInvoiceModel model = document.toObject(BillingInvoiceModel.class);
                                                    saleDetails.add(model);
                                                }
                                            }
                                            callback.onCallback(saleDetails);
                                        }
                                    });
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

    public void getAllBillingInvoices(FirestoreCallback<List<BillingInvoiceModel>> callback) {
        db.collection(DatabaseConstants.INVOICE_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<BillingInvoiceModel> invoices = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                BillingInvoiceModel model = document.toObject(BillingInvoiceModel.class);
                                invoices.add(model);
                            }
                            callback.onCallback(invoices);
                        } else {
                            System.err.println("Error fetching all invoices: " + task.getException());
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

    // ============ TAX MANAGEMENT METHODS ============

    /**
     * Fetch all taxes from Firestore (without requiring composite index)
     */
    public void getTaxList(FirestoreCallback<List<TaxModel>> callback) {
        db.collection(DatabaseConstants.TAXES_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<TaxModel> taxList = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                TaxModel tax = document.toObject(TaxModel.class);
                                if (tax != null) {
                                    if (tax.getTax_id() == null || tax.getTax_id().isEmpty()) {
                                        tax.setTax_id(document.getId());
                                    }
                                    taxList.add(tax);
                                }
                            }
                            // Sort in-memory: active first, then descending percentage, then ascending name
                            Collections.sort(taxList, (t1, t2) -> {
                                boolean a1 = t1.getIs_active() != null && t1.getIs_active();
                                boolean a2 = t2.getIs_active() != null && t2.getIs_active();
                                if (a1 != a2) return a1 ? -1 : 1;
                                double p1 = t1.getTax_percentage() != null ? t1.getTax_percentage() : 0.0;
                                double p2 = t2.getTax_percentage() != null ? t2.getTax_percentage() : 0.0;
                                int cmp = Double.compare(p2, p1);
                                if (cmp != 0) return cmp;
                                String n1 = t1.getTax_name() != null ? t1.getTax_name() : "";
                                String n2 = t2.getTax_name() != null ? t2.getTax_name() : "";
                                return n1.compareToIgnoreCase(n2);
                            });
                            callback.onCallback(taxList);
                        } else {
                            if (task.getException() != null) {
                                System.err.println("Error fetching taxes: " + task.getException().getMessage());
                            }
                            callback.onCallback(new ArrayList<>());
                        }
                    }
                });
    }

    /**
     * Fetch tax by ID
     */
    public void getTaxById(String tax_id, FirestoreCallback<TaxModel> callback) {
        if (tax_id == null || tax_id.trim().isEmpty()) {
            callback.onCallback(null);
            return;
        }

        db.collection(DatabaseConstants.TAXES_COLLECTION)
                .document(tax_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                TaxModel tax = document.toObject(TaxModel.class);
                                if (tax != null) {
                                    if (tax.getTax_id() == null || tax.getTax_id().isEmpty()) {
                                        tax.setTax_id(document.getId());
                                    }
                                    callback.onCallback(tax);
                                    return;
                                }
                            }
                            callback.onCallback(null);
                        } else {
                            if (task.getException() != null) {
                                System.err.println("Error fetching tax: " + task.getException().getMessage());
                            }
                            callback.onCallback(null);
                        }
                    }
                });
    }

    /**
     * Add new tax record
     */
    public void addTax(TaxModel tax, FirestoreCallback<String> callback) {
        if (tax == null || tax.getTax_name() == null || tax.getTax_percentage() == null) {
            callback.onCallback(null);
            return;
        }

        DocumentReference docRef = db.collection(DatabaseConstants.TAXES_COLLECTION).document();
        String id = docRef.getId();
        tax.setTax_id(id);

        long currentTime = System.currentTimeMillis();
        tax.setCreated_at(currentTime);
        tax.setUpdated_at(currentTime);

        docRef.set(tax)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onCallback(id);
                        } else {
                            if (task.getException() != null) {
                                System.err.println("Error adding tax: " + task.getException().getMessage());
                            }
                            callback.onCallback(null);
                        }
                    }
                });
    }

    /**
     * Update existing tax record
     */
    public void updateTax(String tax_id, TaxModel tax, FirestoreCallback<Boolean> callback) {
        if (tax_id == null || tax_id.trim().isEmpty() || tax == null) {
            callback.onCallback(false);
            return;
        }

        tax.setTax_id(tax_id);
        tax.setUpdated_at(System.currentTimeMillis());

        db.collection(DatabaseConstants.TAXES_COLLECTION)
                .document(tax_id)
                .set(tax)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onCallback(true);
                        } else {
                            if (task.getException() != null) {
                                System.err.println("Error updating tax: " + task.getException().getMessage());
                            }
                            callback.onCallback(false);
                        }
                    }
                });
    }

    /**
     * Delete tax record from Firestore
     */
    public void deleteTax(String tax_id, FirestoreCallback<Boolean> callback) {
        if (tax_id == null || tax_id.trim().isEmpty()) {
            callback.onCallback(false);
            return;
        }

        db.collection(DatabaseConstants.TAXES_COLLECTION)
                .document(tax_id)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onCallback(true);
                        } else {
                            if (task.getException() != null) {
                                System.err.println("Error deleting tax: " + task.getException().getMessage());
                            }
                            callback.onCallback(false);
                        }
                    }
                });
    }

}
