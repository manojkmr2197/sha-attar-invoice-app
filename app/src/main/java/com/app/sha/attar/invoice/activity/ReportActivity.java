package com.app.sha.attar.invoice.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.ReportViewAdapter;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.ReportModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.ReportGenerator;
import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ReportActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;

    Button search_bt;
    Spinner typeSpinner;

    List<BillingInvoiceModel> billingInvoiceModelList= new ArrayList<>();;
    DBUtil dbObj;

    List<ReportModel> reportProductList = new ArrayList<>();
    List<ReportModel> reportAccessoriesList = new ArrayList<>();
    List<ReportModel> displayList = new ArrayList<>();

    private static final int REQUEST_WRITE_PERMISSION = 786;

    RadioGroup reportRadioGroup;
    RadioButton productRadio,nonProductRadio;
    LinearLayout data_ll,no_data_ll;
    RecyclerView reportRecyclerView;

    ReportViewAdapter reportViewAdapter;

    private void getBillingInvoiceModel(Long startTime, Long endTime) {

        dbObj.getBillingInvoiceDetail(new FirestoreCallback<List<BillingInvoiceModel>>() {
            @Override
            public void onCallback(List<BillingInvoiceModel> result) {

                if(result.isEmpty()){
                    Toast.makeText(ReportActivity.this, "No bill Data found .!", Toast.LENGTH_LONG).show();
                    return;
                }
                billingInvoiceModelList.clear();
                billingInvoiceModelList.addAll(result);
                Toast.makeText(ReportActivity.this, "Report Data Loaded .!", Toast.LENGTH_LONG).show();

                prepareProductReport(billingInvoiceModelList);
                prepareNonProductReport(billingInvoiceModelList);
                data_ll.setVisibility(View.VISIBLE);
                no_data_ll.setVisibility(View.GONE);
                productRadio.setChecked(true);
                nonProductRadio.setChecked(false);

                displayList.clear();
                displayList.addAll(reportProductList);
                reportViewAdapter.notifyDataSetChanged();

            }
        }, startTime, endTime);

    }

    private void prepareNonProductReport(List<BillingInvoiceModel> billingInvoiceModelList) {
        reportProductList.clear();
        Map<String, ReportGenerator.AggregatedData> reportData = ReportGenerator.getSalesReportData(billingInvoiceModelList);

        reportData.entrySet().stream().forEach(entry->{
            ReportModel report = new ReportModel();
            report.setName(entry.getKey());
            report.setActualPrice(entry.getValue().actualPrice);
            report.setQuantity(entry.getValue().quantity);
            report.setProfit(entry.getValue().profit);
            report.setSoldPrice(entry.getValue().soldPrice);
            reportProductList.add(report);
        });

    }

    private void prepareProductReport(List<BillingInvoiceModel> billingInvoiceModelList) {
        reportAccessoriesList.clear();

        Map<String, ReportGenerator.AccessoryAggregatedData> reportData = ReportGenerator.getAccessoriesReportData(billingInvoiceModelList);

        reportData.entrySet().stream().forEach(entry->{
            ReportModel report = new ReportModel();
            report.setName(entry.getKey());
            report.setActualPrice(entry.getValue().actualPrice);
            report.setQuantity(entry.getValue().quantity);
            report.setProfit(entry.getValue().profit);
            report.setSoldPrice(entry.getValue().soldPrice);
            reportAccessoriesList.add(report);
        });
    }

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

        dbObj = new DBUtil();

        data_ll = (LinearLayout) findViewById(R.id.report_data_ll);
        no_data_ll = (LinearLayout) findViewById(R.id.report_no_data_ll);

        data_ll.setVisibility(View.GONE);
        no_data_ll.setVisibility(View.VISIBLE);

        reportRadioGroup = (RadioGroup) findViewById(R.id.report_radio_group);
        productRadio = (RadioButton) findViewById(R.id.report_product_radio);
        nonProductRadio = (RadioButton) findViewById(R.id.report_non_product_radio);

        reportRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Find which radio button is selected
                displayList.clear();
                if (R.id.report_product_radio == checkedId) {
                    if(reportProductList.isEmpty()){
                        Toast.makeText(ReportActivity.this, "No Product bill Data found .!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    displayList.addAll(reportProductList);
                } else if (R.id.report_non_product_radio == checkedId) {
                    if(reportProductList.isEmpty()){
                        Toast.makeText(ReportActivity.this, "No Accessories bill Data found .!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    displayList.addAll(reportAccessoriesList);
                }
                reportViewAdapter.notifyDataSetChanged();
            }
        });

        reportRecyclerView = (RecyclerView) findViewById(R.id.report_recyclerView);
        reportViewAdapter = new ReportViewAdapter(context,displayList);
        reportRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportRecyclerView.setAdapter(reportViewAdapter);

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        if (R.id.report_back == view.getId()) {
            finish();
        } else if (R.id.report_download_fab == view.getId()) {
            downloadReportStatus();
        } else if (R.id.report_search == view.getId()) {
            LocalDate today = LocalDate.now();
            OffsetDateTime startOfDay = today.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime endOfDay = today.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

            if (typeSpinner.getSelectedItemPosition() == 0) {
                System.out.println(startOfDay + " --- " + endOfDay);
            } else if (typeSpinner.getSelectedItemPosition() == 1) {
                startOfDay = startOfDay.minusDays(1);
                endOfDay = endOfDay.minusDays(1);
            } else if (typeSpinner.getSelectedItemPosition() == 2) {
                startOfDay = startOfDay.minusWeeks(1);
            } else if (typeSpinner.getSelectedItemPosition() == 3) {
                startOfDay = startOfDay.minusMonths(1);
            }
            processReport(startOfDay, endOfDay);
        }
    }

    private void processReport(OffsetDateTime startOfDay, OffsetDateTime endOfDay) {
        System.out.println(startOfDay + " --- " + endOfDay);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getBillingInvoiceModel(startOfDay.toEpochSecond(), endOfDay.toEpochSecond());
        }
    }

    private void downloadReportStatus() {
        if (billingInvoiceModelList.isEmpty()) {
            Toast.makeText(ReportActivity.this, "Since report not generated. generate the report and download.", Toast.LENGTH_LONG).show();
        } else {
            downloadReport();
        }
    }

    private void downloadReport() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        } else {
            try {
                saveExcelFile(billingInvoiceModelList);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ReportActivity.this, "Internal Server Error. Please try again later.!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveExcelFile(List<BillingInvoiceModel> billingInvoiceModelList) throws Exception {
        String fileName = "report-"+System.currentTimeMillis()+".xlsx";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        ReportGenerator.createExcelReport(billingInvoiceModelList, file);

        // Notify the user
        Toast.makeText(this, "Report Generated: " + fileName, Toast.LENGTH_LONG).show();

        // Use FileProvider to get the URI
        Uri fileUri = FileProvider.getUriForFile(this, "com.app.sha.attar.invoice.fileprovider" , file);

        // Open the file using a file explorer
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.ms-excel");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with saving the file
            try {
                saveExcelFile(billingInvoiceModelList);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ReportActivity.this, "Internal Server Error. Please try again later.!", Toast.LENGTH_LONG).show();
            }
        }
    }
}