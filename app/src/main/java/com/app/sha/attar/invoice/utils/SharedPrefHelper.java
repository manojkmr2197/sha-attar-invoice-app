package com.app.sha.attar.invoice.utils;

import static com.app.sha.attar.invoice.utils.SharedConstants.ACCESSORIES_KEY;
import static com.app.sha.attar.invoice.utils.SharedConstants.PACKAGING_KEY;
import static com.app.sha.attar.invoice.utils.SharedConstants.PAYMENT_PAYEE;
import static com.app.sha.attar.invoice.utils.SharedConstants.PAYMENT_URI;
import static com.app.sha.attar.invoice.utils.SharedConstants.PERFUME_100ML_MIXER;
import static com.app.sha.attar.invoice.utils.SharedConstants.PERFUME_10ML_MIXER;
import static com.app.sha.attar.invoice.utils.SharedConstants.PERFUME_30ML_MIXER;
import static com.app.sha.attar.invoice.utils.SharedConstants.PERFUME_50ML_MIXER;
import static com.app.sha.attar.invoice.utils.SharedConstants.PRODUCT_KEY;
import static com.app.sha.attar.invoice.utils.SharedConstants.SHA_ATTAR;


import android.content.Context;
import android.content.SharedPreferences;

import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.ConfigModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.model.SalesPersonModel;
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

    public String getSystemTime(){
        return sharedPreferences.getString(SharedConstants.SYSTEM_TIME, null);
    }

    public void setSystemTime(String dateTime){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedConstants.SYSTEM_TIME, dateTime);
        editor.apply();
        editor.commit();
    }

    public String getLoginUserType(){
        return sharedPreferences.getString(SharedConstants.LOGIN_USER_TYPE, null);
    }

    public String getLoginUserName(){
        return sharedPreferences.getString(SharedConstants.LOGIN_USER_NAME, null);
    }
    public String getLoginUserPhone(){
        return sharedPreferences.getString(SharedConstants.LOGIN_USER_PHONE, null);
    }

    public void setLoginUserDetails(SalesPersonModel salesPersonModel){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedConstants.LOGIN_USER_NAME, salesPersonModel.getName());
        editor.putString(SharedConstants.LOGIN_USER_PHONE, salesPersonModel.getPhoneNo());
        editor.putString(SharedConstants.LOGIN_USER_TYPE, salesPersonModel.getType());
        editor.apply();
        editor.commit();
    }

    public void clearLoginUserDetails(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SharedConstants.LOGIN_USER_NAME);
        editor.remove(SharedConstants.LOGIN_USER_PHONE);
        editor.remove(SharedConstants.LOGIN_USER_TYPE);
        editor.apply();
        editor.commit();
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

    public ConfigModel getPerfumeActualMix(){
        ConfigModel configModel = new ConfigModel();
        configModel.setPerfume10mlMixer(sharedPreferences.getInt(PERFUME_10ML_MIXER, 4));
        configModel.setPerfume30mlMixer(sharedPreferences.getInt(PERFUME_30ML_MIXER, 12));
        configModel.setPerfume50mlMixer(sharedPreferences.getInt(PERFUME_50ML_MIXER, 20));
        configModel.setPerfume100mlMixer(sharedPreferences.getInt(PERFUME_100ML_MIXER, 35));
        return configModel;
    }

    public void setPerfumeActualMix(ConfigModel configModel){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PERFUME_10ML_MIXER, configModel.getPerfume10mlMixer());
        editor.putInt(PERFUME_30ML_MIXER, configModel.getPerfume30mlMixer());
        editor.putInt(PERFUME_50ML_MIXER, configModel.getPerfume50mlMixer());
        editor.putInt(PERFUME_100ML_MIXER, configModel.getPerfume100mlMixer());
        editor.apply();
        editor.commit();
    }

    public String getUpiId() {
        return sharedPreferences.getString(SharedConstants.PAYMENT_URI, null);
    }

    public String getPayeeName() {
        return sharedPreferences.getString(SharedConstants.PAYMENT_PAYEE, null);
    }

    public void setPaymentURIAndName(String paymentUri, String payeeName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PAYMENT_URI, paymentUri);
        editor.putString(PAYMENT_PAYEE, payeeName);
        editor.apply();
        editor.commit();
    }
}
