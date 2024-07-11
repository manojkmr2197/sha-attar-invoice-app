package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.app.sha.attar.invoice.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.OffsetDateTime;


public class ReportActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;

    Button search_bt;
    Spinner typeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        context = ReportActivity.this;
        activity = ReportActivity.this;

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }

        TextView back = (TextView) findViewById(R.id.report_back);
        back.setOnClickListener(this);

        FloatingActionButton download_fab = (FloatingActionButton) findViewById(R.id.report_download_fab);
        download_fab.setOnClickListener(this);

        typeSpinner = (Spinner) findViewById(R.id.report_type_owner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_report_type, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        search_bt = (Button) findViewById(R.id.report_search);
        search_bt.setOnClickListener(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        if (R.id.report_back == view.getId()) {
            finish();
        } else if (R.id.report_download_fab == view.getId()) {
            downloadReportStatus();
        } else if (R.id.report_search == view.getId()){

            LocalDate today = LocalDate.now();
            OffsetDateTime startOfDay = today.atStartOfDay().atOffset(ZoneOffset.UTC);;
            OffsetDateTime endOfDay = today.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

            if(typeSpinner.getSelectedItemPosition() == 0){
                System.out.println(startOfDay+" --- "+endOfDay);
            }else if (typeSpinner.getSelectedItemPosition() == 1){
                startOfDay = startOfDay.minusDays(1);
                endOfDay = endOfDay.minusDays(1);
            }else if (typeSpinner.getSelectedItemPosition() == 2){
                startOfDay = startOfDay.minusWeeks(1);
            }else if (typeSpinner.getSelectedItemPosition() == 3){
                startOfDay = startOfDay.minusMonths(1);
            }
            processReport(startOfDay,endOfDay);
        }
    }

    private void processReport(OffsetDateTime startOfDay, OffsetDateTime endOfDay) {
        System.out.println(startOfDay+" --- "+endOfDay);
    }

    private void downloadReportStatus() {


    }
}