package com.app.sha.attar.invoice.utils;

import static com.app.sha.attar.invoice.utils.SharedConstants.ACCESSORIES_KEY;
import static com.app.sha.attar.invoice.utils.SharedConstants.ADMIN_PASSWORD;
import static com.app.sha.attar.invoice.utils.SharedConstants.PACKAGING_KEY;
import static com.app.sha.attar.invoice.utils.SharedConstants.PRODUCT_KEY;
import static com.app.sha.attar.invoice.utils.SharedConstants.SHA_ATTAR;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;

public class SharedPrefHelper {

    private SharedPreferences sharedPreferences;
    private Gson gson;
    private DBUtil dbObj;

    public SharedPrefHelper(Context context) {
        this.sharedPreferences = context.getSharedPreferences(SHA_ATTAR, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.dbObj = new DBUtil();
    }

    public String getPackageCost(){
        return sharedPreferences.getString(PACKAGING_KEY, "15");
    }

    public void setPackageCost(int amount){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PACKAGING_KEY, String.valueOf(amount));
        editor.apply();
        editor.commit();
    }

    public String getPassword(){
        return sharedPreferences.getString(ADMIN_PASSWORD, "123456");
    }

    public void setPassword(String password){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ADMIN_PASSWORD, password);
        editor.apply();
        editor.commit();
    }

    public List<ProductModel> getTotalProductList(){

        List<ProductModel> arrayItems = new ArrayList<>();
        String serializedObject = sharedPreferences.getString(PRODUCT_KEY, null);
        if (serializedObject != null) {
            Type type = new TypeToken<List<ProductModel>>(){}.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }
        return arrayItems;
    }


    public void setTotalProductItem(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        dbObj.getProductDetails(new FirestoreCallback<List<ProductModel>>() {
            @Override
            public void onCallback(List<ProductModel> products) {
                System.out.println("Number of products-- " + products.size());
                String json = gson.toJson(products);
                editor.putString(PRODUCT_KEY, json);
                editor.apply();
                editor.commit();
            }
        });
    }

    public List<AccessoriesModel> getTotalAccessoriesList(){

        List<AccessoriesModel> arrayItems = new ArrayList<>();

        String serializedObject = sharedPreferences.getString(ACCESSORIES_KEY, null);
        if (serializedObject != null) {
            Type type = new TypeToken<List<AccessoriesModel>>(){}.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }
        return arrayItems;
    }


    public void setTotalAccessoriesItem(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        dbObj.getAllAccessories(new FirestoreCallback<List<AccessoriesModel>>() {
            @Override
            public void onCallback(List<AccessoriesModel> accessories) {
                System.out.println("Number of Accessories-- " + accessories.size());
                String json = gson.toJson(accessories);
                editor.putString(ACCESSORIES_KEY, json);
                editor.apply();
                editor.commit();
            }
        });
    }



}
