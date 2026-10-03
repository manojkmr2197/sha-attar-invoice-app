package com.app.sha.attar.invoice.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

/**
 * Migration helper to ensure all existing products (18% HSN 33029090)
 * and accessories (12% HSN 96161000) have valid Tax and HSN metadata in Firestore.
 */
public class DataMigrationHelper {

    private static final String PREF_MIGRATION_KEY = "is_gst_data_migrated_v2";

    public static void runMigrationIfNeeded(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences("app_migration_prefs", Context.MODE_PRIVATE);
        boolean isMigrated = prefs.getBoolean(PREF_MIGRATION_KEY, false);

        if (!isMigrated) {
            runProductsMigration(context, prefs);
            runAccessoriesMigration(context, prefs);
        }
    }

    public static void forceMigration(Context context, FirestoreCallback<Boolean> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Migrate Products
        db.collection(DatabaseConstants.PRODUCTS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        WriteBatch batch = db.batch();
                        int count = 0;
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            String taxId = doc.getString("tax_id");
                            Double gst = doc.getDouble("gstPercentage");
                            String hsn = doc.getString("hsnCode");

                            if (taxId == null || taxId.isEmpty() || gst == null || gst <= 0 || hsn == null || hsn.isEmpty()) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("tax_id", "tax_perfume_18");
                                updates.put("gstPercentage", 18.0);
                                updates.put("hsnCode", "33029090");
                                batch.update(doc.getReference(), updates);
                                count++;
                            }
                        }
                        if (count > 0) {
                            final int finalCount = count;
                            batch.commit().addOnCompleteListener(task -> {
                                System.out.println("Migrated " + finalCount + " products to 18% GST");
                            });
                        }
                    }

                    // Migrate Accessories
                    db.collection(DatabaseConstants.ACCESSORIES_COLLECTION)
                            .get()
                            .addOnSuccessListener(accSnapshot -> {
                                if (accSnapshot != null && !accSnapshot.isEmpty()) {
                                    WriteBatch accBatch = db.batch();
                                    int accCount = 0;
                                    for (DocumentSnapshot doc : accSnapshot.getDocuments()) {
                                        String taxId = doc.getString("tax_id");
                                        Double gst = doc.getDouble("gstPercentage");
                                        String hsn = doc.getString("hsnCode");

                                        if (taxId == null || taxId.isEmpty() || gst == null || gst <= 0 || hsn == null || hsn.isEmpty()) {
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("tax_id", "tax_accessories_12");
                                            updates.put("gstPercentage", 12.0);
                                            updates.put("hsnCode", "96161000");
                                            accBatch.update(doc.getReference(), updates);
                                            accCount++;
                                        }
                                    }
                                    if (accCount > 0) {
                                        final int finalAccCount = accCount;
                                        accBatch.commit().addOnCompleteListener(task -> {
                                            System.out.println("Migrated " + finalAccCount + " accessories to 12% GST");
                                            if (callback != null) callback.onCallback(true);
                                        });
                                    } else {
                                        if (callback != null) callback.onCallback(true);
                                    }
                                } else {
                                    if (callback != null) callback.onCallback(true);
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onCallback(false);
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onCallback(false);
                });
    }

    private static void runProductsMigration(Context context, SharedPreferences prefs) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(DatabaseConstants.PRODUCTS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        WriteBatch batch = db.batch();
                        int count = 0;
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            String taxId = doc.getString("tax_id");
                            Double gst = doc.getDouble("gstPercentage");
                            String hsn = doc.getString("hsnCode");

                            if (taxId == null || taxId.isEmpty() || gst == null || gst <= 0 || hsn == null || hsn.isEmpty()) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("tax_id", "tax_perfume_18");
                                updates.put("gstPercentage", 18.0);
                                updates.put("hsnCode", "33029090");
                                batch.update(doc.getReference(), updates);
                                count++;
                            }
                        }
                        if (count > 0) {
                            final int finalCount = count;
                            batch.commit().addOnSuccessListener(aVoid -> {
                                System.out.println("Auto-migrated " + finalCount + " products with default 18% GST");
                            });
                        }
                    }
                });
    }

    private static void runAccessoriesMigration(Context context, SharedPreferences prefs) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(DatabaseConstants.ACCESSORIES_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        WriteBatch batch = db.batch();
                        int count = 0;
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            String taxId = doc.getString("tax_id");
                            Double gst = doc.getDouble("gstPercentage");
                            String hsn = doc.getString("hsnCode");

                            if (taxId == null || taxId.isEmpty() || gst == null || gst <= 0 || hsn == null || hsn.isEmpty()) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("tax_id", "tax_accessories_12");
                                updates.put("gstPercentage", 12.0);
                                updates.put("hsnCode", "96161000");
                                batch.update(doc.getReference(), updates);
                                count++;
                            }
                        }
                        if (count > 0) {
                            final int finalCount = count;
                            batch.commit().addOnSuccessListener(aVoid -> {
                                System.out.println("Auto-migrated " + finalCount + " accessories with default 12% GST");
                                prefs.edit().putBoolean(PREF_MIGRATION_KEY, true).apply();
                            });
                        } else {
                            prefs.edit().putBoolean(PREF_MIGRATION_KEY, true).apply();
                        }
                    }
                });
    }
}
