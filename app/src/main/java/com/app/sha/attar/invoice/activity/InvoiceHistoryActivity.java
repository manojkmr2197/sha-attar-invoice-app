package com.app.sha.attar.invoice.activity;

import android.app.Activity;
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
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }
        context = InvoiceHistoryActivity.this;
        activity = InvoiceHistoryActivity.this;

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
        data_recycler_view = (RecyclerView) findViewById(R.id.invoice_history_recyclerView);

        Intent intent = getIntent();
        boolean owner = intent.getBooleanExtra("owner",false);
        if(!owner){
            filter_ll.setVisibility(View.GONE);
            LocalDate today = LocalDate.now();
            startOfDay = today.atStartOfDay().atOffset(ZoneOffset.UTC).minusDays(1);
            endOfDay = today.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
            getInvoiceRecords(startOfDay, endOfDay);
        }

    }

    private void getInvoiceRecords(OffsetDateTime startOfDay, OffsetDateTime endOfDay) {

        //Invoice DB call

        if (contentList.isEmpty()) {
            Toast.makeText(context, "Products is empty. Please try again .! ", Toast.LENGTH_LONG).show();
            no_data_ll.setVisibility(View.VISIBLE);
            data_ll.setVisibility(View.GONE);
            return;
        } else {
            data_ll.setVisibility(View.VISIBLE);
            no_data_ll.setVisibility(View.GONE);
        }


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
            getInvoiceRecords(startOfDay, endOfDay);
        } else if(R.id.invoice_history_add_fab == view.getId()){
            //call intent to  invoice detail activity
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
                    startOfDay = localDate.atTime(LocalTime.MIN).atOffset(ZoneOffset.UTC);
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

                    endOfDay = localDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
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