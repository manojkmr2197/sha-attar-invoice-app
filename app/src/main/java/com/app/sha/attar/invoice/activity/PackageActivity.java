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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.ConfigModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.DatabaseConstants;
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

public class PackageActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;

    TextInputEditText amount_et,perfume_10ml,perfume_30ml,perfume_50ml,perfume_100ml,whatsapp_content;

    SharedPrefHelper helper;

    FirebaseFirestore db;

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
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        helper = new SharedPrefHelper(context);
        db = DBUtil.getInstance();
        amount_et = (TextInputEditText) findViewById(R.id.packaging_amount_et);

        perfume_10ml = (TextInputEditText) findViewById(R.id.packaging_perfume_10ml);
        perfume_30ml = (TextInputEditText) findViewById(R.id.packaging_perfume_30ml);
        perfume_50ml = (TextInputEditText) findViewById(R.id.packaging_perfume_50ml);
        perfume_100ml = (TextInputEditText) findViewById(R.id.packaging_perfume_100ml);
        whatsapp_content = (TextInputEditText) findViewById(R.id.packaging_whatsappContent);

        amount_et.setText(String.valueOf(helper.getPackageCost()));
        ConfigModel configModel = helper.getPerfumeActualMix();
        perfume_10ml.setText(String.valueOf(configModel.getPerfume10mlMixer()));
        perfume_30ml.setText(String.valueOf(configModel.getPerfume30mlMixer()));
        perfume_50ml.setText(String.valueOf(configModel.getPerfume50mlMixer()));
        perfume_100ml.setText(String.valueOf(configModel.getPerfume100mlMixer()));
        whatsapp_content.setText(helper.getWhatsappShareContent());

        Button submit = (Button) findViewById(R.id.packaging_save);
        submit.setOnClickListener(this);
        TextView back = (TextView) findViewById(R.id.packaging_back);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (R.id.packaging_back == v.getId()) {
            finish();
        } else if (R.id.packaging_save == v.getId()) {
            if (checkInternet()) {
                updateDatabase();
                helper.setPackageCost(Integer.parseInt(amount_et.getText().toString()));
                helper.setWhatsappShareContent(whatsapp_content.getText().toString());
                Toast.makeText(this, "Updated..!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean checkInternet() {
        if (SingleTon.isNetworkConnected(activity)) {
            return true;
        } else {
            Toast.makeText(context, "No Internet connection. Please try again .! ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    private void updateDatabase() {
        ConfigModel configModel =new ConfigModel();
        configModel.setPackageCost(Integer.parseInt(amount_et.getText().toString()));
        configModel.setWhatsappContent(whatsapp_content.getText().toString());
        configModel.setPerfume10mlMixer(Integer.parseInt(perfume_10ml.getText().toString()));
        configModel.setPerfume30mlMixer(Integer.parseInt(perfume_30ml.getText().toString()));
        configModel.setPerfume50mlMixer(Integer.parseInt(perfume_50ml.getText().toString()));
        configModel.setPerfume100mlMixer(Integer.parseInt(perfume_100ml.getText().toString()));
        helper.setPerfumeActualMix(configModel);
        db.collection(DatabaseConstants.APP_CONFIG_COLLECTION)
                .document(DatabaseConstants.APP_CONFIG_DOCUMENT)
                .set(configModel)
                .addOnSuccessListener(aVoid -> System.out.println("Configuration updated: "))
                .addOnFailureListener(e -> System.err.println("Configuration not updated. It's failed"));

    }
}
