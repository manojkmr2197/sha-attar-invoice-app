package com.app.sha.attar.invoice.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.ReportViewAdapter;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.model.ReportModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.DatabaseConstants;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.ReportGenerator;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class ReportActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;

    Button search_bt;
    Spinner typeSpinner;

    TextView startDatetv, endDatetv, history_start_date;
    OffsetDateTime customStartDt = null, customEndDt = null, historyStartDt = null;

    List<BillingInvoiceModel> billingInvoiceModelList = new ArrayList<>();

    DBUtil dbObj;

    List<ReportModel> reportProductList = new ArrayList<>();
    List<ReportModel> reportAccessoriesList = new ArrayList<>();
    List<ReportModel> displayList = new ArrayList<>();

    private static final int REQUEST_WRITE_PERMISSION = 786;

    RadioGroup reportRadioGroup;
    RadioButton productRadio, nonProductRadio;
    LinearLayout data_ll, no_data_ll;
    RecyclerView reportRecyclerView;

    ReportViewAdapter reportViewAdapter;

    OffsetDateTime startOfDay, endOfDay;

    private void getBillingInvoiceModel(Long startTime, Long endTime) {

        dbObj.getBillingInvoiceDetail(new FirestoreCallback<List<BillingInvoiceModel>>() {
            @Override
            public void onCallback(List<BillingInvoiceModel> result) {

                if (result.isEmpty()) {
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

    private void prepareProductReport(List<BillingInvoiceModel> billingInvoiceModelList) {
        reportProductList.clear();

        for (BillingInvoiceModel invoice : billingInvoiceModelList) {
            for (BillingItemModel item : invoice.getBillingItemModelList()) {
                if (!item.getType().equals("PRODUCT")) {
                    continue;
                }
                reportProductList.add(getReportModel(item, invoice));
            }
        }

        double totalActual = 0.0;
        double totalSold = 0.0;
        double totalProfit = 0.0;
        int totalQuantity = 0;
        for (ReportModel data : reportProductList) {
            totalActual += data.getActualPrice();
            totalSold += data.getSoldPrice();
            totalProfit += data.getProfit();
            totalQuantity += data.getQuantity();
        }
        ReportModel report = new ReportModel();
        report.setName("Total");
        report.setActualPrice(totalActual);
        report.setQuantity(totalQuantity);
        report.setProfit(totalProfit);
        report.setSoldPrice(totalSold);
        reportProductList.add(report);

    }

    private void prepareNonProductReport(List<BillingInvoiceModel> billingInvoiceModelList) {
        reportAccessoriesList.clear();

        for (BillingInvoiceModel invoice : billingInvoiceModelList) {
            for (BillingItemModel item : invoice.getBillingItemModelList()) {
                if (!item.getType().equals("NON_PRODUCT")) {
                    continue;
                }
                reportAccessoriesList.add(getReportModel(item, invoice));
            }
        }

        double totalActual = 0.0;
        double totalSold = 0.0;
        double totalProfit = 0.0;
        int totalQuantity = 0;
        for (ReportModel data : reportAccessoriesList) {
            totalActual += data.getActualPrice();
            totalSold += data.getSoldPrice();
            totalProfit += data.getProfit();
            totalQuantity += data.getQuantity();
        }
        ReportModel report = new ReportModel();
        report.setName("Total");
        report.setActualPrice(totalActual);
        report.setQuantity(totalQuantity);
        report.setProfit(totalProfit);
        report.setSoldPrice(totalSold);
        reportAccessoriesList.add(report);
    }

    private ReportModel getReportModel(BillingItemModel item, BillingInvoiceModel invoice) {
        double soldPrice = item.getSellingItemPrice() - (item.getSellingItemPrice() * (invoice.getDiscount() / 100));
        double actualPrice = item.getTotalPrice();
        double profit = soldPrice - actualPrice;
        ReportModel report = new ReportModel();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);
            OffsetDateTime offsetDateTime = Instant.ofEpochSecond(invoice.getBillingDate()).atOffset(istOffset);
            report.setDate(offsetDateTime.format(formatter));
        }
        report.setInvoiceId(invoice.getBillingDate());
        report.setName(item.getName());
        report.setActualPrice(actualPrice);
        report.setQuantity((item.getUnits() != null) ? item.getUnits() : 1);
        report.setProfit(profit);
        report.setSoldPrice(item.getSellingItemPrice() - (item.getSellingItemPrice() * (invoice.getDiscount() / 100)));
        return report;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
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

        FloatingActionButton history_delete_fab = (FloatingActionButton) findViewById(R.id.report_history_delete);
        history_delete_fab.setOnClickListener(this);

        typeSpinner = (Spinner) findViewById(R.id.report_type_owner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_report_type, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        startDatetv = findViewById(R.id.start_date_tv);
        endDatetv = findViewById(R.id.end_date_tv);

        startDatetv.setOnClickListener(view -> showStartDatePickerDialog());
        endDatetv.setOnClickListener(view -> showEndDatePickerDialog());


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
                    if (reportProductList.isEmpty()) {
                        Toast.makeText(ReportActivity.this, "No Product bill Data found .!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    displayList.addAll(reportProductList);
                } else if (R.id.report_non_product_radio == checkedId) {
                    if (reportProductList.isEmpty()) {
                        Toast.makeText(ReportActivity.this, "No Accessories bill Data found .!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    displayList.addAll(reportAccessoriesList);
                }
                reportViewAdapter.notifyDataSetChanged();
            }
        });

        reportRecyclerView = (RecyclerView) findViewById(R.id.report_recyclerView);
        reportViewAdapter = new ReportViewAdapter(context, displayList);
        reportRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportRecyclerView.setAdapter(reportViewAdapter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            // Request the necessary permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO},
                    REQUEST_WRITE_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_PERMISSION);
        }

    }

    private void showStartDatePickerDialog() {

        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ReportActivity.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Update the TextView with the selected date
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    startDatetv.setText(dateFormat.format(calendar.getTime()));
                    LocalDate localDate = calendar.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    // Create OffsetDateTime with 00:00 time
                    customStartDt = localDate.atTime(LocalTime.MIN).atOffset(ZoneOffset.UTC);
                },
                year,
                month,
                day
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();

    }

    private void showEndDatePickerDialog() {

        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ReportActivity.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Update the TextView with the selected date
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    endDatetv.setText(dateFormat.format(calendar.getTime()));
                    LocalDate localDate = calendar.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    customEndDt = localDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
                },
                year,
                month,
                day
        );
        datePickerDialog.getDatePicker().setMinDate((customStartDt != null)?customStartDt.toInstant().toEpochMilli():System.currentTimeMillis());
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();

    }

    private void showHistoryPickerDialog() {

        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ReportActivity.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Update the TextView with the selected date
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    history_start_date.setText(dateFormat.format(calendar.getTime()));
                    LocalDate localDate = calendar.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    // Create OffsetDateTime with 00:00 time
                    historyStartDt = localDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
                },
                year,
                month,
                day
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        if (R.id.report_back == view.getId()) {
            finish();
        } else if (R.id.report_download_fab == view.getId()) {
            downloadReportStatus();
        } else if (R.id.report_history_delete == view.getId()) {
            if(checkInternet())
                deleteHistoryRecords();
        } else if (R.id.report_search == view.getId()) {

            if ((customStartDt != null && customEndDt == null) || (customStartDt == null && customEndDt != null)) {
                Toast.makeText(ReportActivity.this, "Please choose proper custom date range .!", Toast.LENGTH_LONG).show();
                return;
            }
            if (customStartDt != null && customEndDt != null) {
                startOfDay = customStartDt;
                endOfDay = customEndDt;
            } else {
                LocalDate today = LocalDate.now();
                startOfDay = today.atStartOfDay().atOffset(ZoneOffset.UTC);
                endOfDay = today.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

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
            }
            if(checkInternet())
                processReport(startOfDay, endOfDay);
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

    private void deleteHistoryRecords() {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.dialog_report_history_delete);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        history_start_date = dialog.findViewById(R.id.report_history_delete_start_date_tv);
        history_start_date.setOnClickListener(view -> showHistoryPickerDialog());

        Button delete = (Button) dialog.findViewById(R.id.report_history_delete_submit);
        TextView close = (TextView) dialog.findViewById(R.id.report_history_delete_close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(historyStartDt == null){
                    Toast.makeText(ReportActivity.this, "Choose the date range.!", Toast.LENGTH_LONG).show();
                    return;
                }
                askDeleteConfirmation();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void askDeleteConfirmation() {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to proceed?");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Toast.makeText(ReportActivity.this, "History records deleting.!", Toast.LENGTH_LONG).show();
            deleteDBHistoryRecords();
            dialog.dismiss();
        });

        // Set negative button
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void deleteDBHistoryRecords() {
        dbObj.deleteRecordsBefore(new FirestoreCallback<List<DocumentSnapshot>>() {
            @Override
            public void onCallback(List<DocumentSnapshot> result) {

                if (result.isEmpty()) {
                    Toast.makeText(ReportActivity.this, "No records found before the given date..!", Toast.LENGTH_LONG).show();
                    return;
                }

                FirebaseFirestore db = DBUtil.getInstance();
                for (DocumentSnapshot document : result) {
                    db.collection(DatabaseConstants.INVOICE_COLLECTION)
                            .document(document.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> System.out.println("Document deleted: " + document.getId()))
                            .addOnFailureListener(e -> System.err.println("Failed to delete document: " + document.getId()));
                }
                Toast.makeText(ReportActivity.this, "Records deleted successfully..!", Toast.LENGTH_LONG).show();
            }
        },historyStartDt.toEpochSecond());
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
        try {
            Toast.makeText(ReportActivity.this, "Loading.!", Toast.LENGTH_LONG).show();
            saveExcelFile(billingInvoiceModelList);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ReportActivity.this, "Internal Server Error. Please try again later.!", Toast.LENGTH_LONG).show();
        }
    }

    private void saveExcelFile(List<BillingInvoiceModel> billingInvoiceModelList) throws Exception {
        String fileName = "report-" + System.currentTimeMillis() + ".xlsx";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.createExcelReport(billingInvoiceModelList, file, startOfDay, endOfDay);

        // Notify the user
        Toast.makeText(this, "Report Generated: " + fileName, Toast.LENGTH_LONG).show();

        // Use FileProvider to get the URI
        Uri fileUri = FileProvider.getUriForFile(this, "com.app.sha.attar.invoice.fileprovider", file);

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
            Toast.makeText(ReportActivity.this, "Permission Granted .!", Toast.LENGTH_SHORT).show();
            // Permission granted, proceed with saving the file
//            try {
//                saveExcelFile(billingInvoiceModelList);
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(ReportActivity.this, "Internal Server Error. Please try again later.!", Toast.LENGTH_LONG).show();
//            }
        }
    }
}