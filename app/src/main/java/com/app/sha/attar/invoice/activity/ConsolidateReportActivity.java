package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.app.sha.attar.invoice.model.ExpenseModel;
import com.app.sha.attar.invoice.model.SalesPersonModel;
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
import java.util.stream.Collectors;

public class ConsolidateReportActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;
    DBUtil dbObj;

    OffsetDateTime startOfDay;
    OffsetDateTime endOfDay;

    TextView back, reportDate;
    TextView start_tv, end_tv;
    Button submit_bt;

    Spinner salePersonSpinner;

    List<BillingInvoiceModel> billingInvoiceModelList = new ArrayList<>();

    LinearLayout totalExpenseLL, netProfitLL;
    TextView productAttarActual, productSprayActual, accessoriesActual, productAttarSold, productSpraySold, accessoriesSold, productAttarProfit, productSprayProfit, accessoriesProfit, totalActual, totalSold, totalProfit, totalExpense, netProfit, totalPay, payCash, payUpi,courierCount,courierAmount;

    double productAttarActualValue, productSprayActualValue, accessoriesActualValue, productAttarSoldValue, productSpraySoldValue, accessoriesSoldValue, productAttarProfitValue, productSprayProfitValue, accessoriesProfitValue, totalActualValue, totalSoldValue, totalProfitValue, totalExpenseValue, totalPaymentValue, totalCash, totalUpi,courierAmountValue;
    int courierCountValue;
    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    DecimalFormat df = new DecimalFormat("#.00");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);

    List<SalesPersonModel> salesPersonModelList;
    List<String> salesPersonSpinnerList;

    ArrayAdapter<String> salesAdapter;

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
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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

        salePersonSpinner = (Spinner) findViewById(R.id.consolidate_report_sales_spinner);
        salesPersonModelList = new ArrayList<>();
        salesPersonSpinnerList = new ArrayList<>();
        salesPersonSpinnerList.add("ALL");
        salesAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                salesPersonSpinnerList
        );

        salesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        salePersonSpinner.setAdapter(salesAdapter);

        loadSalesPersonInformation();

        reportDate = (TextView) findViewById(R.id.consolidate_report_date);

        productAttarActual = (TextView) findViewById(R.id.consolidate_product_attar_actual);
        productSprayActual = (TextView) findViewById(R.id.consolidate_product_spray_actual);
        accessoriesActual = (TextView) findViewById(R.id.consolidate_accessories_actual);
        productAttarProfit = (TextView) findViewById(R.id.consolidate_product_attar_profit);
        productSprayProfit = (TextView) findViewById(R.id.consolidate_product_spray_profit);
        accessoriesProfit = (TextView) findViewById(R.id.consolidate_accessories_profit);
        productAttarSold = (TextView) findViewById(R.id.consolidate_product_attar_sold);
        productSpraySold = (TextView) findViewById(R.id.consolidate_product_spray_sold);
        accessoriesSold = (TextView) findViewById(R.id.consolidate_accessories_sold);
        totalActual = (TextView) findViewById(R.id.consolidate_total_actual_amount);
        totalProfit = (TextView) findViewById(R.id.consolidate_total_profit);
        totalSold = (TextView) findViewById(R.id.consolidate_total_sold_amount);
        totalExpense = (TextView) findViewById(R.id.consolidate_total_expense);
        netProfit = (TextView) findViewById(R.id.consolidate_net_profit);
        totalExpenseLL = (LinearLayout) findViewById(R.id.consolidate_total_expense_ll);
        netProfitLL = (LinearLayout) findViewById(R.id.consolidate_net_profit_ll);
        totalPay = (TextView) findViewById(R.id.consolidate_payment_total);
        payCash = (TextView) findViewById(R.id.consolidate_payment_cash);
        payUpi = (TextView) findViewById(R.id.consolidate_payment_upi);
        courierCount = (TextView) findViewById(R.id.consolidate_courier_count);
        courierAmount = (TextView) findViewById(R.id.consolidate_courier_amount);

    }

    private void loadSalesPersonInformation() {
        salesPersonModelList.clear();
        dbObj.getSalePersonDetails(new FirestoreCallback<List<SalesPersonModel>>() {
            @Override
            public void onCallback(List<SalesPersonModel> result) {
                salesPersonModelList.addAll(result);
                salesPersonSpinnerList.addAll(result.stream()
                        .map(SalesPersonModel::getName)
                        .collect(Collectors.toList()));
            }
        });
        salesAdapter.notifyDataSetChanged();
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
            if (checkInternet()) {
                totalExpenseLL.setVisibility(View.GONE);
                netProfitLL.setVisibility(View.GONE);
                submit_bt.setClickable(false);
                getTotalExpense(startOfDay.toEpochSecond(), endOfDay.toEpochSecond());
            }
        }
    }

    private void getTotalExpense(long startOfDayValue, long endOfDayValue) {
        totalExpenseValue = 0;
        dbObj.getExpenseDetail(new FirestoreCallback<List<ExpenseModel>>() {
            @Override
            public void onCallback(List<ExpenseModel> result) {
                result.forEach(data -> {
                    totalExpenseValue = totalExpenseValue + data.getAmount();
                });
                totalExpense.setText(numberFormat.format(totalExpenseValue).replace("\u00A0", ""));
                if ("ALL".equalsIgnoreCase(salePersonSpinner.getSelectedItem().toString())) {
                    getInvoiceRecords(startOfDay.toEpochSecond(), endOfDay.toEpochSecond());
                } else {
                    getInvoiceRecords(startOfDay.toEpochSecond(), endOfDay.toEpochSecond(), salePersonSpinner.getSelectedItem().toString());
                }
            }
        }, startOfDayValue, endOfDayValue);

    }

    private void getInvoiceRecords(long startOfDayValue, long endOfDayValue, String salesPerson) {

        String selectedPhone = salesPersonModelList.stream()
                .filter(person -> person.getName().equals(salesPerson))
                .map(SalesPersonModel::getPhoneNo)
                .findFirst()
                .orElse(null);

        dbObj.getBillingInvoiceDetail(new FirestoreCallback<List<BillingInvoiceModel>>() {
            @Override
            public void onCallback(List<BillingInvoiceModel> result) {

                billingInvoiceModelList.clear();
                billingInvoiceModelList.addAll(result);

                updateUIData(billingInvoiceModelList);

            }
        }, startOfDayValue, endOfDayValue, selectedPhone);
    }

    private void getInvoiceRecords(long startOfDayValue, long endOfDayValue) {
        dbObj.getBillingInvoiceDetail(new FirestoreCallback<List<BillingInvoiceModel>>() {
            @Override
            public void onCallback(List<BillingInvoiceModel> result) {

                billingInvoiceModelList.clear();
                billingInvoiceModelList.addAll(result);

                updateUIData(billingInvoiceModelList);

            }
        }, startOfDayValue, endOfDayValue);
    }


    private void updateUIData(List<BillingInvoiceModel> billingInvoiceModelList) {
        reportDate.setText("Report generated on " + startOfDay.format(formatter) + " - " + endOfDay.format(formatter));

        Map<String, ReportGenerator.AggregatedData> productAttarData = ReportGenerator.getAggregatedAttarSalesReportData(billingInvoiceModelList);
        Map<String, ReportGenerator.AggregatedData> productSprayData = ReportGenerator.getAggregatedSpraySalesReportData(billingInvoiceModelList);
        Map<String, ReportGenerator.AccessoryAggregatedData> accessoryData = ReportGenerator.getAggregatedAccessoriesReportData(billingInvoiceModelList);

        productAttarActualValue = 0;
        productAttarSoldValue = 0;
        productAttarProfitValue = 0;
        productSprayActualValue = 0;
        productSpraySoldValue = 0;
        productSprayProfitValue = 0;
        accessoriesActualValue = 0;
        accessoriesSoldValue = 0;
        accessoriesProfitValue = 0;
        totalActualValue = 0;
        totalSoldValue = 0;
        totalProfitValue = 0;

        totalPaymentValue = 0;
        totalCash = 0;
        totalUpi = 0;

        courierAmountValue =0;
        courierCountValue =0;

        productAttarData.forEach((key, value) -> {
            productAttarActualValue += value.actualPrice;
            productAttarSoldValue += value.soldPrice;
            productAttarProfitValue += value.profit;
        });
        productSprayData.forEach((key, value) -> {
            productSprayActualValue += value.actualPrice;
            productSpraySoldValue += value.soldPrice;
            productSprayProfitValue += value.profit;
        });
        accessoryData.forEach((key, value) -> {
            accessoriesActualValue += value.actualPrice;
            accessoriesSoldValue += value.soldPrice;
            accessoriesProfitValue += value.profit;
        });

        billingInvoiceModelList.stream().forEach(data -> {
            if ("CASH".equalsIgnoreCase(data.getPaymentMode())) {
                totalCash = totalCash + data.getSellingCost();
            }
            if ("UPI".equalsIgnoreCase(data.getPaymentMode())) {
                totalUpi = totalUpi + data.getSellingCost();
            }
            if(data.getIsCourier()!=null && data.getIsCourier()){
                courierCountValue = courierCountValue+1;
                courierAmountValue = courierAmountValue+data.getCourierAmount();
            }
        });

        productAttarActual.setText("(A) " + numberFormat.format(productAttarActualValue).replace("\u00A0", ""));
        productAttarSold.setText("(A) " + numberFormat.format(productAttarSoldValue).replace("\u00A0", ""));
        productAttarProfit.setText("(A) " + numberFormat.format(productAttarProfitValue).replace("\u00A0", ""));
        productSprayActual.setText("(S) " + numberFormat.format(productSprayActualValue).replace("\u00A0", ""));
        productSpraySold.setText("(S) " + numberFormat.format(productSpraySoldValue).replace("\u00A0", ""));
        productSprayProfit.setText("(S) " + numberFormat.format(productSprayProfitValue).replace("\u00A0", ""));
        accessoriesActual.setText(numberFormat.format(accessoriesActualValue).replace("\u00A0", ""));
        accessoriesSold.setText(numberFormat.format(accessoriesSoldValue).replace("\u00A0", ""));
        accessoriesProfit.setText(numberFormat.format(accessoriesProfitValue).replace("\u00A0", ""));

        totalActualValue = productAttarActualValue + productSprayActualValue + accessoriesActualValue;
        totalSoldValue = productAttarSoldValue + productSpraySoldValue + accessoriesSoldValue;
        totalProfitValue = productAttarProfitValue + productSprayProfitValue + accessoriesProfitValue;

        totalActual.setText(numberFormat.format(totalActualValue).replace("\u00A0", ""));
        totalSold.setText(numberFormat.format(totalSoldValue).replace("\u00A0", ""));
        totalProfit.setText(numberFormat.format(totalProfitValue).replace("\u00A0", ""));
        netProfit.setText(numberFormat.format(totalProfitValue - totalExpenseValue).replace("\u00A0", ""));

        payCash.setText(numberFormat.format(totalCash).replace("\u00A0", ""));
        payUpi.setText(numberFormat.format(totalUpi).replace("\u00A0", ""));
        totalPaymentValue = totalCash + totalUpi;
        totalPay.setText(numberFormat.format(totalPaymentValue).replace("\u00A0", ""));

        courierCount.setText(numberFormat.format(courierCountValue).replace("\u00A0", ""));
        courierAmount.setText(numberFormat.format(courierAmountValue).replace("\u00A0", ""));

        if ("ALL".equalsIgnoreCase(salePersonSpinner.getSelectedItem().toString())) {
            totalExpenseLL.setVisibility(View.VISIBLE);
            netProfitLL.setVisibility(View.VISIBLE);
        }
        submit_bt.setClickable(true);
        Toast.makeText(ConsolidateReportActivity.this, "Invoice Data Loaded .!", Toast.LENGTH_SHORT).show();

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