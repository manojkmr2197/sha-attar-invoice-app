package com.app.sha.attar.invoice.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.app.sha.attar.invoice.R;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        TextView textView = (TextView) findViewById(R.id.home_nav_text_view);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent i =null;
        mDrawerLayout.closeDrawer(GravityCompat.START);
        if (item.getItemId() == R.id.nav_products){
            i = new Intent(MainActivity.this, ProductActivity.class);
            startActivity(i);
//            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        }else if(item.getItemId() == R.id.nav_accessories){
            i = new Intent(MainActivity.this, AccessoriesActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        }else if(item.getItemId() == R.id.nav_report){
            i = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        }else if(item.getItemId() == R.id.nav_packaging)
        {
            i = new Intent(MainActivity.this, PackageActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        }
        return true;
    }
}