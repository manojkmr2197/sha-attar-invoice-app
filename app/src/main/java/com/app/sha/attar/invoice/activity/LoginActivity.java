package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.SalesPersonModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    Context context;
    Activity activity;

    TextInputEditText phone,password;
    Button login;

    DBUtil dbObj;
    SharedPrefHelper sharedPrefHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_login);
        context = LoginActivity.this;
        activity = LoginActivity.this;

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        dbObj = new DBUtil();
        sharedPrefHelper = new SharedPrefHelper(context);
        phone = (TextInputEditText) findViewById(R.id.login_user_name);
        phone.setText("9585905176");
        password = (TextInputEditText) findViewById(R.id.login_user_password);
        password.setText("1234");
        login = (Button) findViewById(R.id.login_submit);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StringUtils.isBlank(phone.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "Please Enter Phone Number .!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (StringUtils.isBlank(password.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "Please Enter password .!", Toast.LENGTH_LONG).show();
                    return;
                }
                loginValidate();
            }
        });

    }

    private void loginValidate() {
        dbObj.getLoginSalesInfoDetail(new FirestoreCallback<List<SalesPersonModel>>() {
            @Override
            public void onCallback(List<SalesPersonModel> result) {
                if (result.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Login Failed .!", Toast.LENGTH_LONG).show();
                    return;
                }

                if(result.size() > 1){
                    Toast.makeText(LoginActivity.this, "Multiple accounts found. Please reach out admin .!", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!result.get(0).isActive()){
                    Toast.makeText(LoginActivity.this, "Login account is currently inactive .!", Toast.LENGTH_LONG).show();
                    return;
                }

                sharedPrefHelper.setLoginUserDetails(result.get(0));
                Toast.makeText(LoginActivity.this, "Welcome "+result.get(0).getName()+" !", Toast.LENGTH_LONG).show();
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        },phone.getText().toString(),password.getText().toString());
    }
}