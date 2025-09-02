package com.app.sha.attar.invoice.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
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
    TextView reportProductNameTv, reportAssetPriceTv, back;
    Button calcBt;
    CardView responseCardView;

    TextView actualAmount1kgTv, actualAmount500gTv, actualAmount250gTv, actualAmount100gTv;
    TextView totalProfit1kgTv, totalProfit500gTv, totalProfit250gTv, totalProfit100gTv;
    TextView profitPerPerson1kgTv, profitPerPerson500gTv, profitPerPerson250gTv, profitPerPerson100gTv;
    TextView totalPayable1kgTv, totalPayable500gTv, totalPayable250gTv, totalPayable100gTv;


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
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        back = (TextView) findViewById(R.id.asset_biz_back);
        productNameET = (TextInputEditText) findViewById(R.id.asset_biz_productName);
        litrePriceET = (TextInputEditText) findViewById(R.id.asset_biz_litre_price);
        sellingPriceET = (TextInputEditText) findViewById(R.id.asset_biz_selling_price);
        reportProductNameTv = (TextView) findViewById(R.id.asset_biz_result_product_name_tv);
        reportAssetPriceTv = (TextView) findViewById(R.id.asset_biz_result_total_amt_tv);

        responseCardView = (CardView) findViewById(R.id.asset_biz_calc_card_view);
        responseCardView.setVisibility(View.GONE);
        actualAmount1kgTv = (TextView) findViewById(R.id.asset_biz_1kg_actual_amount);
        actualAmount500gTv = (TextView) findViewById(R.id.asset_biz_500g_actual_amount);
        actualAmount250gTv = (TextView) findViewById(R.id.asset_biz_250g_actual_amount);
        actualAmount100gTv = (TextView) findViewById(R.id.asset_biz_100g_actual_amount);
        totalProfit1kgTv = (TextView) findViewById(R.id.asset_biz_1kg_total_profit_amount);
        totalProfit500gTv = (TextView) findViewById(R.id.asset_biz_500g_total_profit_amount);
        totalProfit250gTv = (TextView) findViewById(R.id.asset_biz_250g_total_profit_amount);
        totalProfit100gTv = (TextView) findViewById(R.id.asset_biz_100g_total_profit_amount);
        profitPerPerson1kgTv = (TextView) findViewById(R.id.asset_biz_1kg_profit_per_person);
        profitPerPerson500gTv = (TextView) findViewById(R.id.asset_biz_500g_profit_per_person);
        profitPerPerson250gTv = (TextView) findViewById(R.id.asset_biz_250g_profit_per_person);
        profitPerPerson100gTv = (TextView) findViewById(R.id.asset_biz_100g_profit_per_person);
        totalPayable1kgTv = (TextView) findViewById(R.id.asset_biz_1kg_total_payable);
        totalPayable500gTv = (TextView) findViewById(R.id.asset_biz_500g_total_payable);
        totalPayable250gTv = (TextView) findViewById(R.id.asset_biz_250g_total_payable);
        totalPayable100gTv = (TextView) findViewById(R.id.asset_biz_100g_total_payable);

        calcBt = (Button) findViewById(R.id.asset_biz_calc);
        back.setOnClickListener(this);
        calcBt.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.asset_biz_back) {
            finish();
        } else if (view.getId() == R.id.asset_biz_calc) {
            calculateAssets();
        }
    }

    private void calculateAssets() {
        if (StringUtils.isBlank(litrePriceET.getText().toString())) {
            Toast.makeText(this, "Please Entrer Product price (1 Litre) ..!", Toast.LENGTH_LONG).show();
            return;
        }
        if (StringUtils.isBlank(sellingPriceET.getText().toString())) {
            Toast.makeText(this, "Please Entrer Selling price (6 ML) ..!", Toast.LENGTH_LONG).show();
            return;
        }
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        calcBt.setClickable(false);
        responseCardView.setVisibility(View.VISIBLE);
        if (!StringUtils.isBlank(productNameET.getText().toString())) {
            reportProductNameTv.setText("Product Name : " + productNameET.getText().toString());
        }
        int selling6Ml = Integer.parseInt(sellingPriceET.getText().toString());   //100
        int actual1Litre = Integer.parseInt(litrePriceET.getText().toString());   //10000

        double actual1ML = (double) actual1Litre / 1000;   //10
        double selling1ML = (double) selling6Ml / 6;       //16.6

        double totalProfit = (selling1ML - actual1ML);  // (16.6 - 10) == 6.66
        double profitPerson = totalProfit / 2;  // (16.6 - 10)/2 == 3.33
        double payableAmount = actual1Litre + profitPerson ;  //10000+(3.33*1000)
        reportAssetPriceTv.setText("Your Payable amount : Rs. " + numberFormat.format(payableAmount).replace("\u00A0", ""));

        actualAmount1kgTv.setText(numberFormat.format(actual1ML*1000).replace("\u00A0", ""));
        actualAmount500gTv.setText(numberFormat.format(actual1ML*500).replace("\u00A0", ""));
        actualAmount250gTv.setText(numberFormat.format(actual1ML*250).replace("\u00A0", ""));
        actualAmount100gTv.setText(numberFormat.format(actual1ML*100).replace("\u00A0", ""));

        totalProfit1kgTv.setText(numberFormat.format(totalProfit*1000).replace("\u00A0", ""));
        totalProfit500gTv.setText(numberFormat.format(totalProfit*500).replace("\u00A0", ""));
        totalProfit250gTv.setText(numberFormat.format(totalProfit*250).replace("\u00A0", ""));
        totalProfit100gTv.setText(numberFormat.format(totalProfit*100).replace("\u00A0", ""));

        profitPerPerson1kgTv.setText(numberFormat.format(profitPerson*1000).replace("\u00A0", ""));
        profitPerPerson500gTv.setText(numberFormat.format(profitPerson*500).replace("\u00A0", ""));
        profitPerPerson250gTv.setText(numberFormat.format(profitPerson*250).replace("\u00A0", ""));
        profitPerPerson100gTv.setText(numberFormat.format(profitPerson*100).replace("\u00A0", ""));

        totalPayable1kgTv.setText(numberFormat.format((actual1ML*1000)+(profitPerson*1000)).replace("\u00A0", ""));
        totalPayable500gTv.setText(numberFormat.format((actual1ML*500)+(profitPerson*500)).replace("\u00A0", ""));
        totalPayable250gTv.setText(numberFormat.format((actual1ML*250)+(profitPerson*250)).replace("\u00A0", ""));
        totalPayable100gTv.setText(numberFormat.format((actual1ML*100)+(profitPerson*100)).replace("\u00A0", ""));

        calcBt.setClickable(true);
    }
}