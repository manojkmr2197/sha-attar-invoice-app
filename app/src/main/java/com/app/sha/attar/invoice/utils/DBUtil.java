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
    public Boolean AddProduct(ProductModel aModel) {
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

    public void  GetAllProducts(final FirestoreCallback firestoreCallback){
        db.collection("product_details")
                .get()
                .addOnCompleteListener(task -> {
                    List<ProductModel> itemList = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            System.out.println("Sabeek:Data:");
                            System.out.println(document);
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


    public void UpdateProduct(String documentId , ProductModel aModel) {

        db.collection(DatabaseConstants.PRODUCTS_COLLECTION)
            .document(documentId)
            .set(aModel)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
            .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
    }

    public void DeleteProduct(){}


    public void GetAllAccessories(){}

    public void AddAccessories(){}

    public void UpdateAccessories(){}

    public void DeleteAccessories(){}


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