package com.app.sha.attar.invoice.utils;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import android.util.Log;

import com.app.sha.attar.invoice.model.ProductModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    public Boolean AddProduct(String a_ProductName, String a_ProductPrice, String a_ProductOwner, String a_IsProductAvail){
        AtomicReference<java.lang.Boolean> isObjectAdded = new AtomicReference<>(FALSE);
        Map<String, Object> data = new HashMap<>();
        data.put("name", a_ProductName);
        data.put("price", a_ProductPrice);
        data.put("owner", a_ProductOwner);
        data.put("isavailable",a_IsProductAvail);
        System.out.println("Data :"+a_ProductName+":"+a_ProductPrice+":"+a_ProductOwner+":"+a_IsProductAvail);
        db.collection("product_details")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    isObjectAdded.set(TRUE);
                    System.out.println("Product Added suvccessfully.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error while saving product." + e);
                });
        return isObjectAdded.get();
    }

    public void  GetAllProducts(final FirestoreCallback firestoreCallback){
        db.collection("product_details")
                .get()
                .addOnCompleteListener(task -> {
                    List<ProductModel> itemList = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ProductModel product = document.toObject(ProductModel.class);
                            itemList.add(product);
                        }
                    } else {
                        System.out.println("Error while getting product list: " + task.getException());
                        Log.w("ProductActivity", "Error getting documents.", task.getException());
                    }
                    firestoreCallback.onCallback(itemList);
                });
    }

}
