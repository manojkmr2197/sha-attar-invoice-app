package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.InvoiceHistoryViewAdapter;
import com.app.sha.attar.invoice.listener.BillingClickListener;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.listener.TimeApi;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.TimeResponse;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.DatabaseConstants;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.RetrofitClient;
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InvoiceHistoryActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;

    OffsetDateTime startOfDay;
    OffsetDateTime endOfDay;

    TextView back;
    TextView start_tv,end_tv;
    Button submit_bt;

    LinearLayout filter_ll;
    FrameLayout data_ll,no_data_ll;
    FloatingActionButton add_fab;

    RecyclerView data_recycler_view;

    List<BillingInvoiceModel> contentList =new ArrayList<>();
    InvoiceHistoryViewAdapter adapter;
    BillingClickListener listener;

    DBUtil dbObj;
    FirebaseFirestore db;

    boolean owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_invoice_history);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        context = InvoiceHistoryActivity.this;
        activity = InvoiceHistoryActivity.this;
        dbObj = new DBUtil();
        db  = DBUtil.getInstance();
        back = (TextView) findViewById(R.id.invoice_history_back);
        back.setOnClickListener(this);

        start_tv = (TextView) findViewById(R.id.invoice_history_start_date_tv);
        start_tv.setOnClickListener(this);
        end_tv = (TextView) findViewById(R.id.invoice_history_end_date_tv);
        end_tv.setOnClickListener(this);

        submit_bt = (Button) findViewById(R.id.invoice_history_search);
        submit_bt.setOnClickListener(this);

        filter_ll = (LinearLayout)findViewById(R.id.invoice_history_filter_ll);
        data_ll = (FrameLayout) findViewById(R.id.invoice_history_data_ll);
        no_data_ll = (FrameLayout) findViewById(R.id.invoice_history_no_data_ll);

        add_fab = (FloatingActionButton) findViewById(R.id.invoice_history_add_fab);
        add_fab.setOnClickListener(this);

        Intent intent = getIntent();
        owner= intent.getBooleanExtra("owner",false);

        listener = new BillingClickListener() {
            @Override
            public void click(int index,String type) {

                if(checkInternet() && type.equalsIgnoreCase("EDIT")) {
                    //callDetailActivity(context, filteredList.get(index));
                    System.out.println("clicked item : " + index);
                    Intent i = new Intent(InvoiceHistoryActivity.this, InvoiceHistoryDetailsActivity.class);
                    i.putExtra("invoiceId", String.valueOf(contentList.get(index).getBillingDate()));
                    startActivity(i);
                }else if(checkInternet() && type.equalsIgnoreCase("DELETE")){
                    deleteConfirmationPopup(index);
                }
            }
        };

        data_recycler_view = (RecyclerView) findViewById(R.id.invoice_history_recyclerView);
        adapter = new InvoiceHistoryViewAdapter(context,contentList,listener,owner);
        data_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        data_recycler_view.setAdapter(adapter);

        if(!owner){
            filter_ll.setVisibility(View.GONE);
            getServerDate();
        }
    }

    private void getServerDate() {
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(context);
        OffsetDateTime offsetDateTime = null;
        if(StringUtils.isNotBlank(sharedPrefHelper.getSystemTime())) {
            offsetDateTime = OffsetDateTime.parse(sharedPrefHelper.getSystemTime()).withOffsetSameInstant(ZoneOffset.ofHoursMinutes(5, 30));
        }else {
            offsetDateTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.ofHoursMinutes(5, 30));
        }
        startOfDay = offsetDateTime.withHour(0).withMinute(0).withSecond(0).minusDays(1);
        endOfDay = offsetDateTime.withHour(23).withMinute(59).withSecond(59);
        getInvoiceRecords(startOfDay.toEpochSecond(), endOfDay.toEpochSecond());

    }

    private void deleteConfirmationPopup(int index) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to delete?");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteInvoiceDetail(index);
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

    private void deleteInvoiceDetail(int index) {
        db.collection(DatabaseConstants.INVOICE_COLLECTION)
                .document(String.valueOf(contentList.get(index).getBillingDate()))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(InvoiceHistoryActivity.this, "Invoice item deleted .!", Toast.LENGTH_LONG).show();
                    contentList.remove(index);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->  Toast.makeText(InvoiceHistoryActivity.this, "Invoice deleted failed..!", Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(startOfDay != null && endOfDay != null){
            getInvoiceRecords(startOfDay.toEpochSecond(), endOfDay.toEpochSecond());
        }
    }


    private void getInvoiceRecords(long startOfDay, long endOfDay) {
        dbObj.getBillingInvoiceDetail(new FirestoreCallback<List<BillingInvoiceModel>>() {
            @Override
            public void onCallback(List<BillingInvoiceModel> result) {

                if (result.isEmpty()) {
                    Toast.makeText(InvoiceHistoryActivity.this, "No Invoice Data found .!", Toast.LENGTH_LONG).show();
                    no_data_ll.setVisibility(View.VISIBLE);
                    data_ll.setVisibility(View.GONE);
                    return;
                }
                contentList.clear();
                contentList.addAll(result);
                data_ll.setVisibility(View.VISIBLE);
                no_data_ll.setVisibility(View.GONE);
                Toast.makeText(InvoiceHistoryActivity.this, "Invoice Data Loaded .!", Toast.LENGTH_LONG).show();

                adapter.notifyDataSetChanged();

            }
        }, startOfDay, endOfDay);
    }

    @Override
    public void onClick(View view) {
        if (R.id.invoice_history_back == view.getId()) {
            finish();
        } else if(R.id.invoice_history_start_date_tv == view.getId()){
            showStartDatePickerDialog();
        } else if(R.id.invoice_history_end_date_tv == view.getId()){
            showEndDatePickerDialog();
        } else if(R.id.invoice_history_search == view.getId()){
            if(startOfDay == null){
                Toast.makeText(context, "Select Start date ..!", Toast.LENGTH_LONG).show();
                return;
            }
            if(endOfDay == null){
                Toast.makeText(context, "Select End date ..!", Toast.LENGTH_LONG).show();
                return;
            }
            if (checkInternet())
                getInvoiceRecords(startOfDay.toEpochSecond(), endOfDay.toEpochSecond());
        } else if(R.id.invoice_history_add_fab == view.getId()){
            //call intent to  invoice detail activity
            Intent i = new Intent(InvoiceHistoryActivity.this, InvoiceHistoryDetailsActivity.class);
            i.putExtra("owner",owner);
            startActivity(i);
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

    private void showStartDatePickerDialog() {

        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                InvoiceHistoryActivity.this,
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
                InvoiceHistoryActivity.this,
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
        datePickerDialog.getDatePicker().setMinDate((startOfDay != null)?startOfDay.toInstant().toEpochMilli():System.currentTimeMillis());
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();

    }
}