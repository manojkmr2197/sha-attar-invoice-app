package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.google.android.material.textfield.TextInputEditText;

public class PackageActivity extends AppCompatActivity implements View.OnClickListener{

    Context context;
    Activity activity;

    TextInputEditText amount_et;

    SharedPrefHelper helper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_packaging);
        context = PackageActivity.this;
        activity = PackageActivity.this;
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }
        helper = new SharedPrefHelper(context);

        amount_et = (TextInputEditText) findViewById(R.id.packaging_amount_et);
        amount_et.setText(String.valueOf(helper.getPackageCost()));
        Button submit = (Button) findViewById(R.id.packaging_save);
        submit.setOnClickListener(this);
        TextView back = (TextView) findViewById(R.id.packaging_back);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(R.id.packaging_back == v.getId()){
            finish();
        }else if(R.id.packaging_save == v.getId()){
            helper.setPackageCost(Integer.parseInt(amount_et.getText().toString()));
            finish();
        }
    }
}
