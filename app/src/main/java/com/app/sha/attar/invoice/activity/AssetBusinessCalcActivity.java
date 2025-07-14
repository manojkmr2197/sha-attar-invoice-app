package com.app.sha.attar.invoice.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.sha.attar.invoice.R;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class AssetBusinessCalcActivity extends AppCompatActivity implements View.OnClickListener {

    EditText productNameET, litrePriceET, sellingPriceET;
    TextView reportProductNameTv, reportAssetPriceTv,back;
    Button calcBt;

    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_asset_business_calc);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }
        back =  (TextView) findViewById(R.id.asset_biz_back);
        productNameET =  (TextInputEditText) findViewById(R.id.asset_biz_productName);
        litrePriceET =  (TextInputEditText) findViewById(R.id.asset_biz_litre_price);
        sellingPriceET =  (TextInputEditText) findViewById(R.id.asset_biz_selling_price);
        reportProductNameTv =  (TextView) findViewById(R.id.asset_biz_result_product_name_tv);
        reportAssetPriceTv =  (TextView) findViewById(R.id.asset_biz_result_total_amt_tv);
        calcBt =  (Button) findViewById(R.id.asset_biz_calc);
        back.setOnClickListener(this);
        calcBt.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
       if(view.getId() == R.id.asset_biz_back){
           finish();
       }else if(view.getId() == R.id.asset_biz_calc){
           calculateAssets();
       }
    }

    private void calculateAssets() {
        if(StringUtils.isBlank(litrePriceET.getText().toString())){
            Toast.makeText(this, "Please Entrer Product price (1 Litre) ..!", Toast.LENGTH_LONG).show();
            return;
        }
        if(StringUtils.isBlank(sellingPriceET.getText().toString())){
            Toast.makeText(this, "Please Entrer Selling price (6 ML) ..!", Toast.LENGTH_LONG).show();
            return;
        }

        if(!StringUtils.isBlank(productNameET.getText().toString())){
            reportProductNameTv.setText("Product Name : "+productNameET.getText().toString());
        }
        int selling6Ml = Integer.parseInt(sellingPriceET.getText().toString());   //100
        int actual1Litre = Integer.parseInt(litrePriceET.getText().toString());   //10000

        double actual1ML =  (double) actual1Litre / 1000;   //10
        double selling1ML =  (double) selling6Ml / 6;       //16.6

        double profitPerson = (selling1ML - actual1ML) /2;  // (16.6 - 10)/2 == 3.33
        double payableAmount = actual1Litre + (profitPerson*1000);  //10000+(3.33*1000)
        reportAssetPriceTv.setText("Your Payable amount : Rs. "+numberFormat.format(payableAmount).replace("\u00A0", ""));
    }
}