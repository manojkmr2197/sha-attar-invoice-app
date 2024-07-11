package com.app.sha.attar.invoice.utils;
import static com.app.sha.attar.invoice.utils.DatabaseConstants.SALE_COLLECTION;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import androidx.annotation.NonNull;

import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

//    public void getCustomerDetails(FirestoreCallback<BillingInvoiceModel> callback, String phoneNumber) {
//        // Fetch data from Firestore using the phone number
//
//        db.collection(DatabaseConstants.SALE_COLLECTION).whereEqualTo(DatabaseConstants.USER_PHONE, phoneNumber)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            if (!task.getResult().isEmpty()) {
//                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
//                                BillingInvoiceModel billingInvoiceModel = document.toObject(BillingInvoiceModel.class);
//                                callback.onCallback(billingInvoiceModel);
//                            } else {
//                                System.out.println("No customer found with this phone number.");
//                            }
//                        } else {
//                            System.err.println("Error fetching customer details: " + task.getException());
//                        }
//                    }
//                });
//    }


    public void getBillingInvoiceDetail(FirestoreCallback<List<BillingInvoiceModel>> callback, String phoneNumber) {
        db.collection(SALE_COLLECTION).whereEqualTo(DatabaseConstants.USER_PHONE, phoneNumber)
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

    public void getBillingItemModelDetail(FirestoreCallback<List<BillingItemModel>> callback, String saleId) {

        db.collection(DatabaseConstants.BILLING_COLLECTION).whereEqualTo(DatabaseConstants.SALE_ID, saleId)
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


    public Boolean submitBillInvoice(BillingInvoiceModel itemModel) {
        AtomicReference<Boolean> isObjectAdded = new AtomicReference<>(Boolean.FALSE);
        Map<String, Object> data = new HashMap<>();
        data.put("billingDate", itemModel.getBillingDate());
        data.put("customerName", itemModel.getCustomerName());
        data.put("customerPhone", itemModel.getCustomerPhone());
        data.put("discount", itemModel.getDiscount()); // Additional data specific to the second table
        data.put("saleId", itemModel.getSaleId());
        data.put("sellingCost", itemModel.getSellingCost());
        data.put("totalCost", itemModel.getTotalCost());

        db.collection(DatabaseConstants.SALE_COLLECTION)
                .add(data)
                .addOnSuccessListener(documentReference -> {

                    if(submitBillingItems(itemModel.getBillingItemModelList()) == TRUE)
                    {
                        isObjectAdded.set(Boolean.TRUE);
                        System.out.println("Billing item added to second table successfully.");
                    }else{
                        System.out.println("Billing item NOT added to second table successfully.");
                    }

                })
                .addOnFailureListener(e -> {
                    System.err.println("Error while adding billing item to second table: " + e);
                });
        return isObjectAdded.get();
    }


    public Boolean submitBillingItems(List<BillingItemModel> itemModel) {
        AtomicReference<Boolean> isObjectAdded = new AtomicReference<>(Boolean.FALSE);
        for (int i = 0; i < itemModel.size(); i++) {
            BillingItemModel model = itemModel.get(i);
            Map<String, Object> data = new HashMap<>();
            data.put("code", model.getCode());
            data.put("name", model.getName());
            data.put("saleId", model.getSaleId());
            data.put("totalPrice", model.getTotalPrice());
            data.put("type", model.getType());
            data.put("unitPrice", model.getUnitPrice());
            data.put("units", model.getUnits());
            db.collection(DatabaseConstants.BILLING_COLLECTION)
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        isObjectAdded.set(Boolean.TRUE);
                        System.out.println("Billing item added to second table successfully.");
                    })
                    .addOnFailureListener(e -> {
                        isObjectAdded.set(Boolean.FALSE);
                        System.err.println("Error while adding billing item to second table: " + e);
                    });
        }
        return isObjectAdded.get();
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