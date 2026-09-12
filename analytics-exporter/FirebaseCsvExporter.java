import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Standalone Firebase to CSV Exporter
 * Exports data from 3 Firebase collections to 4 CSV files
 * 
 * Usage: java FirebaseCsvExporter
 * Output: ./output/ folder with 4 CSV files
 */
public class FirebaseCsvExporter {
    
    private static final String PRODUCTS_COLLECTION = "product_details_gen2";
    private static final String ACCESSORIES_COLLECTION = "accessories_details_gen2";
    private static final String INVOICE_COLLECTION = "invoice_gen2";
    private static final String OUTPUT_DIR = "output";
    private static final String SERVICE_ACCOUNT_FILE = "serviceAccountKey.json";
    private static final String PROJECT_ID = "sha-attar-invoice";
    
    private Firestore firestore;
    
    public FirebaseCsvExporter() throws Exception {
        initializeFirebase();
    }
    
    private void initializeFirebase() throws Exception {
        File credentialsFile = new File(SERVICE_ACCOUNT_FILE);
        
        if (!credentialsFile.exists()) {
            throw new FileNotFoundException(
                "❌ Service account file not found: " + SERVICE_ACCOUNT_FILE + "\n" +
                "Get it from: Firebase Console → Project Settings → Service Accounts → Generate New Private Key"
            );
        }
        
        try (FileInputStream serviceAccountStream = new FileInputStream(credentialsFile)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(PROJECT_ID)
                    .build();
            
            // Initialize Firebase App if not already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            
            firestore = FirestoreClient.getFirestore();
            System.out.println("✓ Firebase initialized successfully\n");
        }
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Starting Firebase to CSV Export...\n");
            
            FirebaseCsvExporter exporter = new FirebaseCsvExporter();
            exporter.createOutputDirectory();
            
            // Export all collections
            System.out.println("📦 Exporting Products...");
            exporter.exportProducts();
            
            System.out.println("📦 Exporting Accessories...");
            exporter.exportAccessories();
            
            System.out.println("📦 Exporting Invoices & Items...");
            exporter.exportInvoices();
            
            System.out.println("\n✅ Export completed successfully!");
            System.out.println("📁 Files saved to: " + new File(OUTPUT_DIR).getAbsolutePath());
            
            exporter.firestore.close();
            
        } catch (Exception e) {
            System.err.println("\n❌ Error during export: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createOutputDirectory() throws IOException {
        Path path = Paths.get(OUTPUT_DIR);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
            System.out.println("✓ Created output directory: " + OUTPUT_DIR + "\n");
        }
    }
    
    // ==================== PRODUCTS EXPORT ====================
    private void exportProducts() throws Exception {
        List<QueryDocumentSnapshot> docs = firestore.collection(PRODUCTS_COLLECTION)
                .orderBy("name")
                .get()
                .get()
                .getDocuments();
        
        if (docs.isEmpty()) {
            System.out.println("   ⚠️  No products found");
            return;
        }
        
        List<String> headers = Arrays.asList(
                "id", "name", "code", "price", "owner", "status", "documentId", 
                "dealer", "attarSellingPriceMap", "perfumeSellingPriceMap"
        );
        
        List<List<String>> rows = new ArrayList<>();
        
        for (QueryDocumentSnapshot doc : docs) {
            List<String> row = new ArrayList<>();
            
            row.add(safeGet(doc, "id"));
            row.add(safeGet(doc, "name"));
            row.add(safeGet(doc, "code"));
            row.add(safeGet(doc, "price"));
            row.add(safeGet(doc, "owner"));
            row.add(safeGet(doc, "status"));
            row.add(doc.getId()); // documentId
            row.add(safeGet(doc, "dealer"));
            row.add(mapToString(doc.get("attarSellingPriceMap")));
            row.add(mapToString(doc.get("perfumeSellingPriceMap")));
            
            rows.add(row);
        }
        
        writeCsv(OUTPUT_DIR + "/products.csv", headers, rows);
        System.out.println("   ✓ Exported " + rows.size() + " products");
    }
    
    // ==================== ACCESSORIES EXPORT ====================
    private void exportAccessories() throws Exception {
        List<QueryDocumentSnapshot> docs = firestore.collection(ACCESSORIES_COLLECTION)
                .orderBy("name")
                .get()
                .get()
                .getDocuments();
        
        if (docs.isEmpty()) {
            System.out.println("   ⚠️  No accessories found");
            return;
        }
        
        List<String> headers = Arrays.asList(
                "id", "name", "sellingPrice", "actualPrice", "documentId", 
                "owner", "dealer", "status"
        );
        
        List<List<String>> rows = new ArrayList<>();
        
        for (QueryDocumentSnapshot doc : docs) {
            List<String> row = new ArrayList<>();
            
            row.add(safeGet(doc, "id"));
            row.add(safeGet(doc, "name"));
            row.add(safeGet(doc, "sellingPrice"));
            row.add(safeGet(doc, "actualPrice"));
            row.add(doc.getId()); // documentId
            row.add(safeGet(doc, "owner"));
            row.add(safeGet(doc, "dealer"));
            row.add(safeGet(doc, "status"));
            
            rows.add(row);
        }
        
        writeCsv(OUTPUT_DIR + "/accessories.csv", headers, rows);
        System.out.println("   ✓ Exported " + rows.size() + " accessories");
    }
    
    // ==================== INVOICES & ITEMS EXPORT ====================
    private void exportInvoices() throws Exception {
        List<QueryDocumentSnapshot> invoiceDocs = firestore.collection(INVOICE_COLLECTION)
                .get()
                .get()
                .getDocuments();
        
        if (invoiceDocs.isEmpty()) {
            System.out.println("   ⚠️  No invoices found");
            return;
        }
        
        // Export invoices header
        List<String> invoiceHeaders = Arrays.asList(
                "invoiceId", "billingDate", "totalCost", "discount", "customerName", 
                "customerPhone", "paymentMode", "upiPaymentStatus", "isPrint", 
                "isCourier", "courierAmount", "cardCharges", "sellingCost", "remarks", 
                "clientName", "clientPhoneNo"
        );
        
        List<List<String>> invoiceRows = new ArrayList<>();
        List<List<String>> itemRows = new ArrayList<>();
        List<String> itemHeaders = Arrays.asList(
                "invoiceId", "itemType", "itemName", "itemCode", "units", 
                "unitPrice", "totalPrice", "sellingItemPrice", "productCategory", "pieces"
        );
        
        int itemCount = 0;
        
        for (QueryDocumentSnapshot invoiceDoc : invoiceDocs) {
            String invoiceId = invoiceDoc.getId();
            
            // Create invoice row
            List<String> invoiceRow = new ArrayList<>();
            invoiceRow.add(invoiceId);
            invoiceRow.add(safeGet(invoiceDoc, "billingDate"));
            invoiceRow.add(safeGet(invoiceDoc, "totalCost"));
            invoiceRow.add(safeGet(invoiceDoc, "discount"));
            invoiceRow.add(safeGet(invoiceDoc, "customerName"));
            invoiceRow.add(safeGet(invoiceDoc, "customerPhone"));
            invoiceRow.add(safeGet(invoiceDoc, "paymentMode"));
            invoiceRow.add(safeGet(invoiceDoc, "upiPaymentStatus"));
            invoiceRow.add(safeGet(invoiceDoc, "isPrint"));
            invoiceRow.add(safeGet(invoiceDoc, "isCourier"));
            invoiceRow.add(safeGet(invoiceDoc, "courierAmount"));
            invoiceRow.add(safeGet(invoiceDoc, "cardCharges"));
            invoiceRow.add(safeGet(invoiceDoc, "sellingCost"));
            invoiceRow.add(safeGet(invoiceDoc, "remarks"));
            invoiceRow.add(safeGet(invoiceDoc, "clientName"));
            invoiceRow.add(safeGet(invoiceDoc, "clientPhoneNo"));
            
            invoiceRows.add(invoiceRow);
            
            // Export invoice items
            List<Map<String, Object>> billingItems = (List<Map<String, Object>>) invoiceDoc.get("billingItemModelList");
            if (billingItems != null) {
                for (Map<String, Object> item : billingItems) {
                    List<String> itemRow = new ArrayList<>();
                    itemRow.add(invoiceId);
                    itemRow.add(mapGet(item, "type"));
                    itemRow.add(mapGet(item, "name"));
                    itemRow.add(mapGet(item, "code"));
                    itemRow.add(mapGet(item, "units"));
                    itemRow.add(mapGet(item, "unitPrice"));
                    itemRow.add(mapGet(item, "totalPrice"));
                    itemRow.add(mapGet(item, "sellingItemPrice"));
                    itemRow.add(mapGet(item, "productCategory"));
                    itemRow.add(mapGet(item, "pieces"));
                    
                    itemRows.add(itemRow);
                    itemCount++;
                }
            }
        }
        
        writeCsv(OUTPUT_DIR + "/invoices.csv", invoiceHeaders, invoiceRows);
        System.out.println("   ✓ Exported " + invoiceRows.size() + " invoices");
        
        writeCsv(OUTPUT_DIR + "/invoice_items.csv", itemHeaders, itemRows);
        System.out.println("   ✓ Exported " + itemCount + " invoice items");
    }
    
    // ==================== UTILITY METHODS ====================
    
    private String safeGet(QueryDocumentSnapshot doc, String field) {
        Object value = doc.get(field);
        return value != null ? value.toString() : "";
    }
    
    private String mapGet(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
    
    private String mapToString(Object mapObj) {
        if (mapObj == null || !(mapObj instanceof Map)) {
            return "";
        }
        
        Map<String, Object> map = (Map<String, Object>) mapObj;
        StringBuilder sb = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (sb.length() > 0) sb.append("|");
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        
        return sb.toString();
    }
    
    private void writeCsv(String filePath, List<String> headers, List<List<String>> rows) throws IOException {
        File file = new File(filePath);
        
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            // Write headers
            writer.write(String.join(",", headers));
            writer.write("\n");
            
            // Write data rows
            for (List<String> row : rows) {
                List<String> escapedRow = new ArrayList<>();
                for (String cell : row) {
                    escapedRow.add(escapeCsvValue(cell));
                }
                writer.write(String.join(",", escapedRow));
                writer.write("\n");
            }
        }
    }
    
    private String escapeCsvValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        
        // If contains comma, newline, or quote, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
}
