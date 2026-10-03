package com.app.sha.attar.invoice.activity;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.TaxModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.google.android.material.button.MaterialButton;

import org.apache.commons.lang3.StringUtils;

public class TaxEditActivity extends AppCompatActivity {

    private TextView tax_edit_back;
    private TextView tax_edit_title;
    private EditText tax_name_input;
    private EditText hsn_code_input;
    private EditText tax_percentage_input;
    private EditText tax_description_input;
    private CheckBox is_active_checkbox;
    private MaterialButton btn_save;
    private MaterialButton btn_delete;
    private MaterialButton btn_cancel;
    private ProgressBar progressBar;

    private String tax_id;
    private boolean is_edit = false;
    private TaxModel currentTax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_tax_edit);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        initializeViews();
        loadIntentData();
    }

    private void initializeViews() {
        tax_edit_back = findViewById(R.id.tax_edit_back);
        tax_edit_title = findViewById(R.id.tax_edit_title);
        tax_name_input = findViewById(R.id.tax_name_input);
        hsn_code_input = findViewById(R.id.hsn_code_input);
        tax_percentage_input = findViewById(R.id.tax_percentage_input);
        tax_description_input = findViewById(R.id.tax_description_input);
        is_active_checkbox = findViewById(R.id.is_active_checkbox);
        btn_save = findViewById(R.id.btn_save);
        btn_delete = findViewById(R.id.btn_delete);
        btn_cancel = findViewById(R.id.btn_cancel);
        progressBar = findViewById(R.id.progressBar);

        tax_edit_back.setOnClickListener(v -> finish());
        btn_save.setOnClickListener(v -> saveTax());
        btn_cancel.setOnClickListener(v -> finish());
        btn_delete.setOnClickListener(v -> deleteTaxConfirm());
    }

    private void loadIntentData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            is_edit = extras.getBoolean("is_edit", false);
            tax_id = extras.getString("tax_id", "");

            if (is_edit && !StringUtils.isEmpty(tax_id)) {
                tax_edit_title.setText("Update Tax Rate");
                btn_save.setText("Update");
                btn_delete.setVisibility(View.VISIBLE);
                loadTaxData();
            } else {
                tax_edit_title.setText("Add Tax Rate");
                btn_save.setText("Save");
                btn_delete.setVisibility(View.GONE);
            }
        }
    }

    private void loadTaxData() {
        progressBar.setVisibility(View.VISIBLE);
        DBUtil dbUtil = new DBUtil();
        dbUtil.getTaxById(tax_id, tax -> {
            progressBar.setVisibility(View.GONE);
            if (tax != null) {
                currentTax = tax;
                populateFields(tax);
            } else {
                Toast.makeText(TaxEditActivity.this, "Failed to load tax data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields(TaxModel tax) {
        if (tax_name_input != null) tax_name_input.setText(tax.getTax_name());
        if (hsn_code_input != null) hsn_code_input.setText(tax.getHsn_code());
        if (tax_percentage_input != null) tax_percentage_input.setText(String.valueOf(tax.getTax_percentage()));
        if (tax_description_input != null) tax_description_input.setText(tax.getDescription());
        if (is_active_checkbox != null) is_active_checkbox.setChecked(tax.getIs_active() != null ? tax.getIs_active() : true);
    }

    private boolean validateInputs() {
        if (StringUtils.isEmpty(tax_name_input.getText().toString().trim())) {
            Toast.makeText(this, "Please enter Tax Name", Toast.LENGTH_SHORT).show();
            tax_name_input.requestFocus();
            return false;
        }

        if (StringUtils.isEmpty(hsn_code_input.getText().toString().trim())) {
            Toast.makeText(this, "Please enter HSN Code", Toast.LENGTH_SHORT).show();
            hsn_code_input.requestFocus();
            return false;
        }

        String percentageStr = tax_percentage_input.getText().toString().trim();
        if (StringUtils.isEmpty(percentageStr)) {
            Toast.makeText(this, "Please enter Tax Percentage", Toast.LENGTH_SHORT).show();
            tax_percentage_input.requestFocus();
            return false;
        }

        try {
            double percentage = Double.parseDouble(percentageStr);
            if (percentage < 0 || percentage > 100) {
                Toast.makeText(this, "Tax percentage must be between 0 and 100", Toast.LENGTH_SHORT).show();
                tax_percentage_input.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid tax percentage number", Toast.LENGTH_SHORT).show();
            tax_percentage_input.requestFocus();
            return false;
        }

        return true;
    }

    private void saveTax() {
        if (!validateInputs()) {
            return;
        }

        btn_save.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        String tax_name = tax_name_input.getText().toString().trim();
        String hsn_code = hsn_code_input.getText().toString().trim();
        Double tax_percentage = Double.parseDouble(tax_percentage_input.getText().toString().trim());
        String description = tax_description_input.getText().toString().trim();
        Boolean is_active = is_active_checkbox.isChecked();

        TaxModel tax = new TaxModel(hsn_code, tax_name, tax_percentage, description);
        tax.setIs_active(is_active);

        DBUtil dbUtil = new DBUtil();

        if (is_edit && !StringUtils.isEmpty(tax_id)) {
            tax.setTax_id(tax_id);
            if (currentTax != null) {
                tax.setCreated_at(currentTax.getCreated_at());
                tax.setOwner(currentTax.getOwner());
            }
            // Update existing tax
            dbUtil.updateTax(tax_id, tax, success -> {
                progressBar.setVisibility(View.GONE);
                btn_save.setEnabled(true);
                if (success) {
                    Toast.makeText(TaxEditActivity.this, "Tax updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TaxEditActivity.this, "Failed to update tax. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new tax
            dbUtil.addTax(tax, newTaxId -> {
                progressBar.setVisibility(View.GONE);
                btn_save.setEnabled(true);
                if (newTaxId != null) {
                    Toast.makeText(TaxEditActivity.this, "Tax created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TaxEditActivity.this, "Failed to create tax. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteTaxConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tax")
                .setMessage("Are you sure you want to delete this tax rate?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    btn_delete.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    DBUtil dbUtil = new DBUtil();
                    dbUtil.deleteTax(tax_id, success -> {
                        progressBar.setVisibility(View.GONE);
                        btn_delete.setEnabled(true);
                        if (success) {
                            Toast.makeText(TaxEditActivity.this, "Tax deleted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(TaxEditActivity.this, "Failed to delete tax. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
