package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.sha.attar.invoice.R;

public class PackageActivity extends AppCompatActivity implements View.OnClickListener{

    Context context;
    Activity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packaging);
        context = PackageActivity.this;
        activity = PackageActivity.this;
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }
        TextView back = (TextView) findViewById(R.id.product_back);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(R.id.product_back == v.getId()){
            finish();
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
        }
    }
}
