package com.app.sha.attar.invoice.utils;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.app.sha.attar.invoice.model.TaxModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to manage tax dropdowns in Product and Accessories activities.
 * Fetches active taxes from Firebase and populates spinner with formatted options.
 */
public class TaxSpinnerHelper {

    private Context context;
    private DBUtil dbUtil;
    private Spinner taxSpinner;
    private List<TaxModel> taxModelList;
    private List<String> taxDisplayList;
    private ArrayAdapter<String> adapter;
    private TaxSelectionListener selectionListener;

    private String pendingTaxId = null;
    private Double pendingGstRate = null;

    public interface TaxSelectionListener {
        void onTaxSelected(TaxModel tax);
    }

    public TaxSpinnerHelper(Context context, Spinner taxSpinner, TaxSelectionListener listener) {
        this.context = context;
        this.taxSpinner = taxSpinner;
        this.selectionListener = listener;
        this.dbUtil = new DBUtil();
        this.taxModelList = new ArrayList<>();
        this.taxDisplayList = new ArrayList<>();
    }

    /**
     * Load active taxes from Firebase and populate the spinner.
     */
    public void loadTaxes() {
        dbUtil.getTaxList(new FirestoreCallback<List<TaxModel>>() {
            @Override
            public void onCallback(List<TaxModel> taxes) {
                if (taxes == null || taxes.isEmpty()) {
                    Toast.makeText(context, "No taxes available. Please initialize or create taxes.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Filter active taxes (or if none marked active, show all)
                taxModelList.clear();
                taxDisplayList.clear();
                for (TaxModel tax : taxes) {
                    if (tax.getIs_active() == null || tax.getIs_active()) {
                        taxModelList.add(tax);
                        taxDisplayList.add(formatTaxDisplay(tax));
                    }
                }

                if (taxModelList.isEmpty()) {
                    // Fallback to all taxes if none active
                    for (TaxModel tax : taxes) {
                        taxModelList.add(tax);
                        taxDisplayList.add(formatTaxDisplay(tax));
                    }
                }

                if (taxModelList.isEmpty()) {
                    return;
                }

                // Setup adapter
                adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, taxDisplayList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                taxSpinner.setAdapter(adapter);

                // Set spinner selection listener
                taxSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                        if (position >= 0 && position < taxModelList.size() && selectionListener != null) {
                            selectionListener.onTaxSelected(taxModelList.get(position));
                        }
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    }
                });

                // Apply pending selection if previously requested
                applyPendingSelection();
            }
        });
    }

    private void applyPendingSelection() {
        if (taxModelList.isEmpty() || taxSpinner == null) return;

        if (pendingTaxId != null && !pendingTaxId.isEmpty()) {
            for (int i = 0; i < taxModelList.size(); i++) {
                if (pendingTaxId.equalsIgnoreCase(taxModelList.get(i).getTax_id())) {
                    taxSpinner.setSelection(i);
                    return;
                }
            }
        }

        if (pendingGstRate != null) {
            for (int i = 0; i < taxModelList.size(); i++) {
                if (Double.compare(pendingGstRate, taxModelList.get(i).getTax_percentage()) == 0) {
                    taxSpinner.setSelection(i);
                    return;
                }
            }
        }
    }

    /**
     * Format tax display as: "HSN Code - Tax Name - XX% (Description)"
     */
    private String formatTaxDisplay(TaxModel tax) {
        String hsn = tax.getHsn_code() != null ? tax.getHsn_code() : "";
        String name = tax.getTax_name() != null ? tax.getTax_name() : "";
        double rate = tax.getTax_percentage() != null ? tax.getTax_percentage() : 0.0;
        String desc = tax.getDescription() != null && !tax.getDescription().isEmpty() ? " (" + tax.getDescription() + ")" : "";
        return String.format("%s - %s - %.1f%%%s", hsn, name, rate, desc);
    }

    /**
     * Set tax selection by tax_id (supports calling before or after loadTaxes completes)
     */
    public void setSelectedTaxById(String tax_id) {
        this.pendingTaxId = tax_id;
        applyPendingSelection();
    }

    /**
     * Set tax selection by GST percentage rate (fallback for legacy products/accessories)
     */
    public void setSelectedTaxByRate(Double rate) {
        this.pendingGstRate = rate;
        applyPendingSelection();
    }

    /**
     * Get the currently selected tax model.
     */
    public TaxModel getSelectedTax() {
        if (taxSpinner == null) return null;
        int position = taxSpinner.getSelectedItemPosition();
        if (position >= 0 && position < taxModelList.size()) {
            return taxModelList.get(position);
        }
        return null;
    }
}
