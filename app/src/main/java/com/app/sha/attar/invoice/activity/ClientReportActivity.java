package com.app.sha.attar.invoice.activity;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.ClientAdapter;
import com.app.sha.attar.invoice.adapter.ClientOrdersAdapter;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.ClientModel;
import com.app.sha.attar.invoice.utils.DBUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ClientReportActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView startDatetv, endDatetv, backTv;
    private AutoCompleteTextView searchBar;
    private Button filterBtn;
    private RecyclerView recyclerView;
    private LinearLayout dataLl, noDataLl;

    private DBUtil dbObj;
    private OffsetDateTime customStartDt;
    private OffsetDateTime customEndDt;

    private final List<BillingInvoiceModel> allInvoices = new ArrayList<>();
    private final List<ClientModel> groupedClients = new ArrayList<>();
    private final List<ClientModel> displayClients = new ArrayList<>();
    private ClientAdapter clientAdapter;
    private ArrayAdapter<String> searchSuggestionsAdapter;
    private final List<String> suggestionsList = new ArrayList<>();
    private boolean isFiltered = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_report);

        // Customize status bar to match existing activities
        Window window = getWindow();
        if (window != null) {
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Initialize UI components
        backTv = findViewById(R.id.client_report_back);
        startDatetv = findViewById(R.id.client_report_start_date_tv);
        endDatetv = findViewById(R.id.client_report_end_date_tv);
        searchBar = findViewById(R.id.client_report_search_bar);
        filterBtn = findViewById(R.id.client_report_search_btn);
        recyclerView = findViewById(R.id.client_report_recyclerView);
        dataLl = findViewById(R.id.client_report_data_ll);
        noDataLl = findViewById(R.id.client_report_no_data_ll);

        dbObj = new DBUtil();

        // Listeners
        backTv.setOnClickListener(this);
        startDatetv.setOnClickListener(this);
        endDatetv.setOnClickListener(this);
        filterBtn.setOnClickListener(this);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        clientAdapter = new ClientAdapter(this, displayClients, this::showClientDetailsDialog);
        recyclerView.setAdapter(clientAdapter);

        // Setup Autocomplete Search
        searchSuggestionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestionsList);
        searchBar.setAdapter(searchSuggestionsAdapter);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load data initially
        loadAllInvoices();
    }

    private void loadAllInvoices() {
        Toast.makeText(this, "Loading client data...", Toast.LENGTH_SHORT).show();
        dbObj.getAllBillingInvoices(invoices -> {
            allInvoices.clear();
            if (invoices != null) {
                allInvoices.addAll(invoices);
            }
            groupAndDisplayClients(allInvoices);
        });
    }

    private void groupAndDisplayClients(List<BillingInvoiceModel> invoices) {
        groupedClients.clear();
        Map<String, ClientModel> clientMap = new HashMap<>();

        for (BillingInvoiceModel invoice : invoices) {
            String phone = invoice.getClientPhoneNo();
            if (TextUtils.isEmpty(phone)) {
                // Fallback to customerPhone if clientPhoneNo is empty (for backward compatibility)
                phone = invoice.getCustomerPhone();
            }

            if (TextUtils.isEmpty(phone)) {
                continue;
            }

            String name = invoice.getClientName();
            if (TextUtils.isEmpty(name)) {
                name = invoice.getCustomerName();
            }
            if (TextUtils.isEmpty(name)) {
                name = "Unknown Client";
            }

            ClientModel client = clientMap.get(phone);
            if (client == null) {
                client = new ClientModel(name, phone);
                clientMap.put(phone, client);
            } else {
                // Update client name to latest invoice name if name was unknown
                if ("Unknown Client".equals(client.getName()) && !TextUtils.isEmpty(name)) {
                    client.setName(name);
                }
            }
            client.addInvoice(invoice);
        }

        groupedClients.addAll(clientMap.values());

        // Sort descending by total purchase amount
        Collections.sort(groupedClients, (c1, c2) -> Double.compare(c2.getTotalSpent(), c1.getTotalSpent()));

        // Populate Suggestions list
        suggestionsList.clear();
        Set<String> uniqueSuggestions = new HashSet<>();
        for (ClientModel client : groupedClients) {
            if (!TextUtils.isEmpty(client.getName())) {
                uniqueSuggestions.add(client.getName());
            }
            if (!TextUtils.isEmpty(client.getPhone())) {
                uniqueSuggestions.add(client.getPhone());
            }
        }
        suggestionsList.addAll(uniqueSuggestions);
        searchSuggestionsAdapter.notifyDataSetChanged();

        // Refresh UI
        filterClients(searchBar.getText().toString());
    }

    private void filterClients(String query) {
        List<ClientModel> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        if (lowerQuery.isEmpty()) {
            filtered.addAll(groupedClients);
        } else {
            for (ClientModel client : groupedClients) {
                boolean matchesName = client.getName() != null && client.getName().toLowerCase().contains(lowerQuery);
                boolean matchesPhone = client.getPhone() != null && client.getPhone().toLowerCase().contains(lowerQuery);
                if (matchesName || matchesPhone) {
                    filtered.add(client);
                }
            }
        }

        displayClients.clear();
        displayClients.addAll(filtered);
        clientAdapter.notifyDataSetChanged();

        if (displayClients.isEmpty()) {
            dataLl.setVisibility(View.GONE);
            noDataLl.setVisibility(View.VISIBLE);
        } else {
            dataLl.setVisibility(View.VISIBLE);
            noDataLl.setVisibility(View.GONE);
        }
    }

    private void showClientDetailsDialog(ClientModel client) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_client_details, null);
        builder.setView(dialogView);

        TextView nameTv = dialogView.findViewById(R.id.dialog_client_name);
        TextView phoneTv = dialogView.findViewById(R.id.dialog_client_phone);
        RecyclerView ordersRv = dialogView.findViewById(R.id.dialog_client_orders_recycler);

        nameTv.setText(client.getName());
        phoneTv.setText(client.getPhone());

        ordersRv.setLayoutManager(new LinearLayoutManager(this));
        
        // Pass the client's invoices to adapter
        // We sort invoices by date descending so latest order is on top
        List<BillingInvoiceModel> clientInvoices = new ArrayList<>(client.getInvoices());
        Collections.sort(clientInvoices, (i1, i2) -> {
            Long d1 = i1.getBillingDate();
            Long d2 = i2.getBillingDate();
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            return d2.compareTo(d1);
        });

        ClientOrdersAdapter ordersAdapter = new ClientOrdersAdapter(this, clientInvoices);
        ordersRv.setAdapter(ordersAdapter);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showStartDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    startDatetv.setText(dateFormat.format(calendar.getTime()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalDate localDate = calendar.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        customStartDt = localDate.atTime(LocalTime.MIN).atZone(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
                    } else {
                        // Compatibility logic if needed, setting customStartDt using milliseconds or a Calendar equivalent
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        customStartDt = OffsetDateTime.ofInstant(calendar.toInstant(), ZoneId.of("Asia/Kolkata"));
                    }
                },
                year,
                month,
                day
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showEndDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    endDatetv.setText(dateFormat.format(calendar.getTime()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalDate localDate = calendar.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        customEndDt = localDate.atTime(LocalTime.MAX).atZone(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
                    } else {
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        calendar.set(Calendar.MILLISECOND, 999);
                        customEndDt = OffsetDateTime.ofInstant(calendar.toInstant(), ZoneId.of("Asia/Kolkata"));
                    }
                },
                year,
                month,
                day
        );
        if (customStartDt != null) {
            datePickerDialog.getDatePicker().setMinDate(customStartDt.toInstant().toEpochMilli());
        }
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void processFilter() {
        if (customStartDt == null || customEndDt == null) {
            Toast.makeText(this, "Please select both Start and End Dates", Toast.LENGTH_SHORT).show();
            return;
        }

        long startEpoch = customStartDt.toEpochSecond();
        long endEpoch = customEndDt.toEpochSecond();

        List<BillingInvoiceModel> filteredInvoices = new ArrayList<>();
        for (BillingInvoiceModel invoice : allInvoices) {
            if (invoice.getBillingDate() != null) {
                long billingTime = invoice.getBillingDate();
                if (billingTime >= startEpoch && billingTime <= endEpoch) {
                    filteredInvoices.add(invoice);
                }
            }
        }

        isFiltered = true;
        filterBtn.setText("Reset");
        groupAndDisplayClients(filteredInvoices);
        Toast.makeText(this, "Filtered report loaded", Toast.LENGTH_SHORT).show();
    }

    private void clearFilter() {
        customStartDt = null;
        customEndDt = null;
        startDatetv.setText("Start Date");
        endDatetv.setText("End Date");
        isFiltered = false;
        filterBtn.setText("Filter");
        groupAndDisplayClients(allInvoices);
        Toast.makeText(this, "Filters reset", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.client_report_back) {
            finish();
        } else if (id == R.id.client_report_start_date_tv) {
            showStartDatePickerDialog();
        } else if (id == R.id.client_report_end_date_tv) {
            showEndDatePickerDialog();
        } else if (id == R.id.client_report_search_btn) {
            if (isFiltered) {
                clearFilter();
            } else {
                processFilter();
            }
        }
    }
}
