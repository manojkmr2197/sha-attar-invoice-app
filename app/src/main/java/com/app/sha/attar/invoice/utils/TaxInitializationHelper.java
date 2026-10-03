package com.app.sha.attar.invoice.utils;

import com.app.sha.attar.invoice.model.TaxModel;
import com.google.firebase.firestore.FirebaseFirestore;

public class TaxInitializationHelper {

    private static final String TAX_PERFUME_ID = "tax_perfume_18";
    private static final String TAX_ACCESSORIES_ID = "tax_accessories_12";

    /**
     * Initialize default taxes if they don't exist
     * This should be called once during app startup (e.g., LoginActivity or SplashActivity)
     */
    public static void initializeDefaultTaxes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if tax_perfume_18 exists
        db.collection(DatabaseConstants.TAXES_COLLECTION)
                .document(TAX_PERFUME_ID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Create default perfume tax (18%)
                        TaxModel perfumeTax = new TaxModel(
                                "33029090",  // HSN Code for Perfumes & Attars
                                "Perfumes & Attars",
                                18.0,
                                "All perfume and attar products"
                        );
                        perfumeTax.setTax_id(TAX_PERFUME_ID);
                        perfumeTax.setCreated_at(System.currentTimeMillis());
                        perfumeTax.setUpdated_at(System.currentTimeMillis());

                        db.collection(DatabaseConstants.TAXES_COLLECTION)
                                .document(TAX_PERFUME_ID)
                                .set(perfumeTax)
                                .addOnSuccessListener(aVoid ->
                                        System.out.println("Default perfume tax created: 18% - HSN 33029090")
                                )
                                .addOnFailureListener(e ->
                                        System.err.println("Failed to create perfume tax: " + e.getMessage())
                                );
                    }
                })
                .addOnFailureListener(e ->
                        System.err.println("Error checking perfume tax: " + e.getMessage())
                );

        // Check if tax_accessories_12 exists
        db.collection(DatabaseConstants.TAXES_COLLECTION)
                .document(TAX_ACCESSORIES_ID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Create default accessories tax (12%)
                        TaxModel accessoriesTax = new TaxModel(
                                "96161000",  // HSN Code for Accessories & Packaging
                                "Accessories & Packaging",
                                12.0,
                                "Accessories, bottles, boxes, and packaging materials"
                        );
                        accessoriesTax.setTax_id(TAX_ACCESSORIES_ID);
                        accessoriesTax.setCreated_at(System.currentTimeMillis());
                        accessoriesTax.setUpdated_at(System.currentTimeMillis());

                        db.collection(DatabaseConstants.TAXES_COLLECTION)
                                .document(TAX_ACCESSORIES_ID)
                                .set(accessoriesTax)
                                .addOnSuccessListener(aVoid ->
                                        System.out.println("Default accessories tax created: 12% - HSN 96161000")
                                )
                                .addOnFailureListener(e ->
                                        System.err.println("Failed to create accessories tax: " + e.getMessage())
                                );
                    }
                })
                .addOnFailureListener(e ->
                        System.err.println("Error checking accessories tax: " + e.getMessage())
                );
    }

    /**
     * Get default perfume tax ID (18%)
     */
    public static String getDefaultPerfumeTaxId() {
        return TAX_PERFUME_ID;
    }

    /**
     * Get default accessories tax ID (12%)
     */
    public static String getDefaultAccessoriesTaxId() {
        return TAX_ACCESSORIES_ID;
    }
}
