package com.app.sha.attar.invoice.utils;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import androidx.annotation.NonNull;

import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DBUtil {
    private FirebaseFirestore db;

    public DBUtil(){

        db = FirebaseFirestore.getInstance();
    }

    public Boolean addProduct(ProductModel aModel) {
        AtomicReference<java.lang.Boolean> isObjectAdded = new AtomicReference<>(FALSE);
        db.collection(DatabaseConstants.PRODUCTS_COLLECTION)
                .add(aModel)
                .addOnSuccessListener(documentReference -> {
                    isObjectAdded.set(TRUE);
                    System.out.println("Product Added successfully.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error while saving product." + e);
                });
        return isObjectAdded.get();
    }

    public void getProductDetails(FirestoreCallback<List<ProductModel>> callback) {
        // Fetch data from Firestore
        db.collection(DatabaseConstants.PRODUCTS_COLLECTION)
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

    public Boolean updateProduct(String documentId , ProductModel aModel) {
        AtomicReference<java.lang.Boolean> isObjectAdded = new AtomicReference<>(FALSE);
        db.collection(DatabaseConstants.PRODUCTS_COLLECTION)
            .document(documentId)
            .set(aModel)
                .addOnSuccessListener(documentReference -> {
                    isObjectAdded.set(TRUE);
                    System.out.println("Product Updated successfully.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error while updating product." + e);
                });
        return isObjectAdded.get();
    }

    public Boolean deleteProductById(String documentId, FirestoreCallback<Void> callback) {
        AtomicReference<java.lang.Boolean> isObjectDeleted = new AtomicReference<>(FALSE);
        db.collection(DatabaseConstants.PRODUCTS_COLLECTION).document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Call the callback with null since the task was successful
                        callback.onCallback(aVoid);
                        isObjectDeleted.set(TRUE);
                        System.out.println("Product successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.err.println("Error deleting product: " + e);
                    }
                });
        return isObjectDeleted.get();
    }

    public void getAllAccessories(FirestoreCallback<List<AccessoriesModel>> callback) {
        // Fetch data from Firestore
        db.collection(DatabaseConstants.ACCESSORIES_COLLECTION)
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

    public boolean addAccessories(AccessoriesModel aModel) {
        AtomicReference<java.lang.Boolean> isObjectAdded = new AtomicReference<>(FALSE);
        db.collection(DatabaseConstants.ACCESSORIES_COLLECTION)
                .add(aModel)
                .addOnSuccessListener(documentReference -> {
                    isObjectAdded.set(TRUE);
                    System.out.println("Accessories Added successfully.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error while saving Accessories." + e);
                });
        return isObjectAdded.get();
    }

    public boolean updateAccessories(String documentId , AccessoriesModel aModel) {
        AtomicReference<java.lang.Boolean> isObjectAdded = new AtomicReference<>(FALSE);
        db.collection(DatabaseConstants.ACCESSORIES_COLLECTION)
                .document(documentId)
                .set(aModel)
                .addOnSuccessListener(documentReference -> {
                    isObjectAdded.set(TRUE);
                    System.out.println("Accessories Updated successfully.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error while updating product." + e);
                });
        return isObjectAdded.get();
    }

    public boolean deleteAccessories(String documentId, FirestoreCallback<Void> callback) {
        AtomicReference<java.lang.Boolean> isObjectDeleted = new AtomicReference<>(FALSE);
        db.collection(DatabaseConstants.ACCESSORIES_COLLECTION).document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Call the callback with null since the task was successful
                        callback.onCallback(aVoid);
                        isObjectDeleted.set(TRUE);
                        System.out.println("Accessories successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.err.println("Error deleting Accessories: " + e);
                    }
                });
        return isObjectDeleted.get();
    }

//    public void getCustomerDetail(FirestoreCallback<CustomerDetails> callback, String phoneNumber) {
//        // Fetch data from Firestore using the phone number
//
//        db.collection(DatabaseConstants.CUSTOMER_COLLECTION).whereEqualTo(DatabaseConstants.USER_PHONE, phoneNumber)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            if (!task.getResult().isEmpty()) {
//                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
//                                CustomerDetails customerDetails = document.toObject(CustomerDetails.class);
//                                callback.onCallback(customerDetails);
//                            } else {
//                                System.out.println("No customer found with this phone number.");
//                            }
//                        } else {
//                            System.err.println("Error fetching customer details: " + task.getException());
//                        }
//                    }
//                });
//    }
//
//
//    public void getCustomerHistoryDetail(FirestoreCallback<List<CustomerHistoryModel>> callback, Integer customerId) {
//        // Fetch sale data from Firestore using the customer id
//
//        db.collection(DatabaseConstants.SALE_COLLECTION).whereEqualTo(DatabaseConstants.USER_ID, customerId)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            List<CustomerHistoryModel> saleDetails = new ArrayList<>();
//                            for (DocumentSnapshot document : task.getResult()) {
//                                CustomerHistoryModel model = document.toObject(CustomerHistoryModel.class);
//                                saleDetails.add(model);
//                            }
//                            callback.onCallback(saleDetails);
//                        } else {
//                            System.err.println("Error fetching product details: " + task.getException());
//                        }
//                    }
//                });
//    }
//
//    public void getBillingItemModelDetail(FirestoreCallback<List<BillingItemModel>> callback, Integer saleId) {
//        // Fetch sale data from Firestore using the customer id
//
//        db.collection(DatabaseConstants.BILLING_COLLECTION).whereEqualTo(DatabaseConstants.SALE_ID, saleId)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            List<BillingItemModel> billingItemModel = new ArrayList<>();
//                            for (DocumentSnapshot document : task.getResult()) {
//                                BillingItemModel model = document.toObject(BillingItemModel.class);
//                                billingItemModel.add(model);
//                            }
//                            callback.onCallback(billingItemModel);
//                        } else {
//                            System.err.println("Error fetching product details: " + task.getException());
//                        }
//                    }
//                });
//    }

    public Boolean submitBillInvoice(BillingInvoiceModel aModel) {
        //need to write a logic to save the billing data

        return true;
    }

}
/*
* 1.Add product
	->I/p : model object ,
	  o/p :status Boolean

2.Update product
	I/p : model object , document id
	o/p : status boolean

3.Get All product list
	o/p:All products with document id

4.Get All accessories
	o/p:Accessories with document id

5.Add Accessories
	->I/p : model object ,
	  o/p :status Boolean
6.Update Accessories
	I/p : model object , document id
	o/p : status boolean

7.Delete Product
	i/p: doc id
	o/p: status boolean
8.Delete accessories
	i/p: doc id
	o/p: status boolean

9.BILLING
	i/p :
		Model-->{sale_id:string ,Date:timestamp , Total sold amount :int ,Discount:int,
					ArrayOfProducts{
							{DocId,name,code,unitprice,quantity,soldprice}
					}
					ArrayOfAccessiories{
							{DocId,name,price}
					}
			 }
* */