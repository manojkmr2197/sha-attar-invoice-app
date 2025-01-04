package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.ReportGenerator;
import com.app.sha.attar.invoice.utils.SingleTon;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import java.util.Map;

public class ConsolidateReportActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;
    DBUtil dbObj;

    OffsetDateTime startOfDay;
    OffsetDateTime endOfDay;

    TextView back,reportDate;
    TextView start_tv, end_tv;
    Button submit_bt;

    List<BillingInvoiceModel> billingInvoiceModelList = new ArrayList<>();

    TextView productActual, accessoriesActual,productSold, accessoriesSold, productProfit, accessoriesProfit, totalActual,totalSold, totalProfit, profitPerPerson, amountFromIk;

    double productActualValue,accessoriesActualValue,productSoldValue, accessoriesSoldValue, productProfitValue, accessoriesProfitValue, totalActualValue,totalSoldValue, totalProfitValue;

    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    DecimalFormat df = new DecimalFormat("#.00");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_consolidate_report);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }
        context = ConsolidateReportActivity.this;
        activity = ConsolidateReportActivity.this;
        dbObj = new DBUtil();

        back = (TextView) findViewById(R.id.consolidate_back);
        back.setOnClickListener(this);
        start_tv = (TextView) findViewById(R.id.consolidate_start_date_tv);
        start_tv.setOnClickListener(this);
        end_tv = (TextView) findViewById(R.id.consolidate_end_date_tv);
        end_tv.setOnClickListener(this);
        submit_bt = (Button) findViewById(R.id.consolidate_search);
        submit_bt.setOnClickListener(this);

        reportDate = (TextView) findViewById(R.id.consolidate_report_date);

        productActual = (TextView) findViewById(R.id.consolidate_product_actual);
        accessoriesActual = (TextView) findViewById(R.id.consolidate_accessories_actual);
        productProfit = (TextView) findViewById(R.id.consolidate_product_profit);
        accessoriesProfit = (TextView) findViewById(R.id.consolidate_accessories_profit);
        productSold = (TextView) findViewById(R.id.consolidate_product_sold);
        accessoriesSold = (TextView) findViewById(R.id.consolidate_accessories_sold);
        totalActual = (TextView) findViewById(R.id.consolidate_total_actual_amount);
        totalProfit = (TextView) findViewById(R.id.consolidate_total_profit);
        totalSold = (TextView) findViewById(R.id.consolidate_total_sold_amount);
        profitPerPerson = (TextView) findViewById(R.id.consolidate_profit_per_person);
        amountFromIk = (TextView) findViewById(R.id.consolidate_amount_from_ik);

    }

    @Override
    public void onClick(View view) {
        if (R.id.consolidate_back == view.getId()) {
            finish();
        } else if (R.id.consolidate_start_date_tv == view.getId()) {
            showStartDatePickerDialog();
        } else if (R.id.consolidate_end_date_tv == view.getId()) {
            showEndDatePickerDialog();
        } else if (R.id.consolidate_search == view.getId()) {
            if (startOfDay == null) {
                Toast.makeText(context, "Select Start date ..!", Toast.LENGTH_LONG).show();
                return;
            }
            if (endOfDay == null) {
                Toast.makeText(context, "Select End date ..!", Toast.LENGTH_LONG).show();
                return;
            }
            if (checkInternet())
                getInvoiceRecords(startOfDay.toEpochSecond(), endOfDay.toEpochSecond());
        }
    }

    private void getInvoiceRecords(long startOfDayValue, long endOfDayValue) {
        dbObj.getBillingInvoiceDetail(new FirestoreCallback<List<BillingInvoiceModel>>() {
            @Override
            public void onCallback(List<BillingInvoiceModel> result) {

                if (result.isEmpty()) {
                    Toast.makeText(ConsolidateReportActivity.this, "No Invoice found .!", Toast.LENGTH_LONG).show();
                    return;
                }
                billingInvoiceModelList.clear();
                billingInvoiceModelList.addAll(result);


                reportDate.setText("Report generated on "+startOfDay.format(formatter)+" - "+endOfDay.format(formatter));

                Map<String, ReportGenerator.AggregatedData> productData = ReportGenerator.getAggregatedSalesReportData(billingInvoiceModelList);
                Map<String, ReportGenerator.AccessoryAggregatedData> accessoryData = ReportGenerator.getAggregatedAccessoriesReportData(billingInvoiceModelList);

                productActualValue =0;
                productSoldValue =0;
                productProfitValue =0;
                accessoriesActualValue =0;
                accessoriesSoldValue =0;
                accessoriesProfitValue =0;

                productData.forEach((key,value)->{
                    productActualValue +=value.actualPrice;
                    productSoldValue +=value.soldPrice;
                    productProfitValue +=value.profit;
                });
                accessoryData.forEach((key,value)->{
                    accessoriesActualValue +=value.actualPrice;
                    accessoriesSoldValue +=value.soldPrice;
                    accessoriesProfitValue +=value.profit;
                });

                productActual.setText(numberFormat.format(productActualValue).replace("\u00A0", ""));
                productSold.setText(numberFormat.format(productSoldValue).replace("\u00A0", ""));
                productProfit.setText(numberFormat.format(productProfitValue).replace("\u00A0", ""));
                accessoriesActual.setText(numberFormat.format(accessoriesActualValue).replace("\u00A0", ""));
                accessoriesSold.setText(numberFormat.format(accessoriesSoldValue).replace("\u00A0", ""));
                accessoriesProfit.setText(numberFormat.format(accessoriesProfitValue).replace("\u00A0", ""));

                totalActualValue = productActualValue+accessoriesActualValue;
                totalSoldValue = productSoldValue+accessoriesSoldValue;
                totalProfitValue = productProfitValue+accessoriesProfitValue;

                totalActual.setText(numberFormat.format(totalActualValue).replace("\u00A0", ""));
                totalSold.setText(numberFormat.format(totalSoldValue).replace("\u00A0", ""));
                totalProfit.setText(numberFormat.format(totalProfitValue).replace("\u00A0", ""));
                profitPerPerson.setText(numberFormat.format(totalProfitValue/2).replace("\u00A0", ""));
                amountFromIk.setText(numberFormat.format(totalActualValue+(totalProfitValue/2)).replace("\u00A0", ""));

                Toast.makeText(ConsolidateReportActivity.this, "Invoice Data Loaded .!", Toast.LENGTH_SHORT).show();
            }
        }, startOfDayValue, endOfDayValue);
    }

    private boolean checkInternet() {
        if (SingleTon.isNetworkConnected(activity)) {
            return true;
        } else {
            Toast.makeText(context, "No Internet connection. Please try again .! ", Toast.LENGTH_LONG).show();
            return false;
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
                ConsolidateReportActivity.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Update the TextView with the selected date
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    start_tv.setText(dateFormat.format(calendar.getTime()));
                    LocalDate localDate = calendar.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    // Create OffsetDateTime with 00:00 time
                    startOfDay = localDate.atTime(LocalTime.MIN).atZone(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
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
                ConsolidateReportActivity.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Update the TextView with the selected date
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    end_tv.setText(dateFormat.format(calendar.getTime()));
                    LocalDate localDate = calendar.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    endOfDay = localDate.atTime(LocalTime.MAX).atZone(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
                },
                year,
                month,
                day
        );
        datePickerDialog.getDatePicker().setMinDate((startOfDay != null) ? startOfDay.toInstant().toEpochMilli() : System.currentTimeMillis());
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();

    }
}