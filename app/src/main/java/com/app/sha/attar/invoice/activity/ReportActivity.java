package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.ReportGenerator;
import com.app.sha.attar.invoice.utils.SharedConstants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;


public class ReportActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;

    Button search_bt;
    Spinner typeSpinner;

    Long prevStartTime,prevEndTime;
    List<BillingInvoiceModel>billingInvoiceModelList;
    DBUtil dbObj;

    private void getBillingInvoiceModel(Long startTime,Long endTime){
        if(!Objects.equals(startTime, prevStartTime) || !Objects.equals(endTime, prevEndTime) || billingInvoiceModelList.isEmpty()){

            dbObj.getBillingInvoiceDetail(new FirestoreCallback<List<BillingInvoiceModel>>() {
                @Override
                public void onCallback(List<BillingInvoiceModel> result) {
                    System.out.println("SabeekResultSize:"+result.size());
                    billingInvoiceModelList.clear();
                    billingInvoiceModelList=result;
                }
            },startTime,endTime);

        }

    }
    public ReportActivity(){
        this.prevEndTime=0L;
        this.prevStartTime=0L;
        billingInvoiceModelList = new ArrayList<>();
        dbObj = new DBUtil();
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

        preAuthentication();

    }

    private void preAuthentication() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Admin Credential");

        // Set up the input
        final EditText input = new EditText(this);
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString();
                if(StringUtils.isEmpty(inputText) || !SharedConstants.ADMIN_PASSWORD.equals(inputText)){
                    Toast.makeText(ReportActivity.this, "Wrong Password. try again later.!", Toast.LENGTH_LONG).show();
                    finish();
                    dialog.cancel();
                    return;
                }
                Toast.makeText(ReportActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                dialog.cancel();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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
            OffsetDateTime startOfDay = today.atStartOfDay().atOffset(ZoneOffset.UTC);
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
        getBillingInvoiceModel(startOfDay.toEpochSecond(),endOfDay.toEpochSecond());
    }

    private void downloadReportStatus() {
        if(billingInvoiceModelList.isEmpty()){
            Toast.makeText(ReportActivity.this, "Since report not generated generate the report and download." , Toast.LENGTH_LONG).show();

        }else {
            //manojRet();
            downloadProductReport();
        }

    }
    private void downloadProductReport(){
        System.out.println("Sabeek:length:"+billingInvoiceModelList.size());
        try {
            /*
            List<BillingItemModel>bimodel=new ArrayList<>();
            bimodel.add(new BillingItemModel("NON","SAHA","CODE1",25,10,250));
            bimodel.add(new BillingItemModel("NON","GAKA","CODE1",25,10,250));
            bimodel.add(new BillingItemModel("NON","OHAM","CODE1",25,10,250));
            bimodel.add(new BillingItemModel("NON","AAHA","CODE1",25,10,250));

            List<BillingInvoiceModel> invoices = new ArrayList<> ();
            BillingInvoiceModel bilinmod=new BillingInvoiceModel( 122334L,620,10,"Guru","7904385250",730);

            invoices.add(bilinmod);
            */
            File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDirectory.exists()) {
                downloadsDirectory.mkdirs();
            }
            Long timeStr = System.currentTimeMillis();
            File file = new File(downloadsDirectory, "invoice_"+timeStr+".xls");
            ReportGenerator.createExcelReport(billingInvoiceModelList, file);
            Toast.makeText(ReportActivity.this, "Report exported in Downloads. " , Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ReportActivity.this, "Error creating CSV file", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ReportActivity.this, "Unexpected error occurred", Toast.LENGTH_SHORT).show();
        }
    }
}