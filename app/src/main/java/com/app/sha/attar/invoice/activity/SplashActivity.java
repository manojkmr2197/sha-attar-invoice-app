package com.app.sha.attar.invoice.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.ConfigModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.DatabaseConstants;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.app.sha.attar.invoice.utils.SingleTon;

import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private static int TIME_OUT = 3000;

    DBUtil dbUtil;
    SharedPrefHelper helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_splash);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }
        dbUtil = new DBUtil();
        helper = new SharedPrefHelper(SplashActivity.this);
        if(checkInternet()) {
            loadAppConfig();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }, TIME_OUT);
        }else{
            finish();
        }

    }

    private boolean checkInternet() {
        if (SingleTon.isNetworkConnected(SplashActivity.this)) {
            return true;
        } else {
            Toast.makeText(SplashActivity.this, "No Internet connection. Please try again .! ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    private void loadAppConfig() {
        dbUtil.getAppConfig(new FirestoreCallback<ConfigModel>() {
            @Override
            public void onCallback(ConfigModel result) {
                if(result != null) {
                    helper.setPackageCost(result.getPackageCost());
                    helper.setPassword(result.getAdminPassword());
                }else{
                    helper.setPackageCost(15);
                    helper.setPassword("123456");
                }
            }
        });
    }
}