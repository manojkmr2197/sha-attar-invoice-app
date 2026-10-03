package com.app.sha.attar.invoice.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.TaxAdapter;
import com.app.sha.attar.invoice.model.TaxModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TaxManagementActivity extends AppCompatActivity {

    private RecyclerView taxRecyclerView;
    private TaxAdapter taxAdapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddTax;
    private TextView backButton;
    private EditText searchEditText;
    private FrameLayout noDataLayout;
    private FrameLayout dataLayout;

    private List<TaxModel> fullTaxList = new ArrayList<>();
    private List<TaxModel> displayedTaxList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_tax_management);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        initializeViews();
        loadTaxes();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.tax_back);
        searchEditText = findViewById(R.id.tax_search_et);
        taxRecyclerView = findViewById(R.id.tax_recyclerView);
        progressBar = findViewById(R.id.tax_progress_bar);
        fabAddTax = findViewById(R.id.tax_add_fab);
        noDataLayout = findViewById(R.id.tax_no_data_ll);
        dataLayout = findViewById(R.id.tax_data_ll);

        backButton.setOnClickListener(v -> finish());

        // Setup RecyclerView
        taxRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taxAdapter = new TaxAdapter(this, displayedTaxList, tax -> {
            Intent intent = new Intent(TaxManagementActivity.this, TaxEditActivity.class);
            intent.putExtra("tax_id", tax.getTax_id());
            intent.putExtra("is_edit", true);
            startActivity(intent);
        });
        taxRecyclerView.setAdapter(taxAdapter);

        // FAB click listener
        fabAddTax.setOnClickListener(v -> {
            Intent intent = new Intent(TaxManagementActivity.this, TaxEditActivity.class);
            intent.putExtra("is_edit", false);
            startActivity(intent);
        });

        // Search text watcher
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTaxes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterTaxes(String query) {
        displayedTaxList.clear();
        if (query == null || query.trim().isEmpty()) {
            displayedTaxList.addAll(fullTaxList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (TaxModel tax : fullTaxList) {
                boolean nameMatch = tax.getTax_name() != null && tax.getTax_name().toLowerCase().contains(lowerQuery);
                boolean hsnMatch = tax.getHsn_code() != null && tax.getHsn_code().toLowerCase().contains(lowerQuery);
                boolean descMatch = tax.getDescription() != null && tax.getDescription().toLowerCase().contains(lowerQuery);
                if (nameMatch || hsnMatch || descMatch) {
                    displayedTaxList.add(tax);
                }
            }
        }
        taxAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (displayedTaxList.isEmpty()) {
            noDataLayout.setVisibility(View.VISIBLE);
            dataLayout.setVisibility(View.GONE);
        } else {
            noDataLayout.setVisibility(View.GONE);
            dataLayout.setVisibility(View.VISIBLE);
        }
    }

    private void loadTaxes() {
        progressBar.setVisibility(View.VISIBLE);
        DBUtil dbUtil = new DBUtil();
        dbUtil.getTaxList(taxes -> {
            progressBar.setVisibility(View.GONE);
            fullTaxList.clear();
            displayedTaxList.clear();
            if (taxes != null && !taxes.isEmpty()) {
                fullTaxList.addAll(taxes);
                String currentQuery = searchEditText != null ? searchEditText.getText().toString() : "";
                if (!currentQuery.trim().isEmpty()) {
                    filterTaxes(currentQuery);
                } else {
                    displayedTaxList.addAll(taxes);
                    taxAdapter.notifyDataSetChanged();
                    updateEmptyState();
                }
            } else {
                taxAdapter.notifyDataSetChanged();
                updateEmptyState();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTaxes();
    }
}
