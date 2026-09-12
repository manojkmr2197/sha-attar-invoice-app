# 🚀 Firebase CSV Exporter - Quick Start

## Step 1: Get Firebase Service Account Credentials

1. Go to: **https://console.firebase.google.com/**
2. Select your project: **sha-attar-invoice**
3. Click ⚙️ **Settings** (top-left) → **Project Settings**
4. Go to **Service Accounts** tab
5. Click **Generate New Private Key** (Java option)
6. **Save** the downloaded JSON file

## Step 2: Place Service Account File

Copy the downloaded JSON file to the **analytics-exporter folder** and **rename it to**:
```
serviceAccountKey.json
```

**Folder structure should look like:**
```
analytics-exporter/
├── FirebaseCsvExporter.java
├── pom.xml
├── serviceAccountKey.json    ← Place the credential file here
├── QUICK_START.md
└── README.md
```

## Step 3: Install Dependencies & Run

### Option A: Using Maven (Recommended)

```bash
# Navigate to analytics-exporter folder
cd analytics-exporter

# Compile the project
mvn clean compile

# Run the exporter
mvn exec:java -Dexec.mainClass="FirebaseCsvExporter"
```

### Option B: Create Fat JAR and Run

```bash
# Build standalone JAR with all dependencies
cd analytics-exporter
mvn clean package

# Run the JAR (generates output folder in current directory)
java -jar target/firebase-csv-exporter.jar
```

### Option C: Using Gradle

```bash
cd analytics-exporter

# Create build.gradle file (if not present)
gradle init --type java-application

# Run the exporter
gradle run
```

## Step 4: Check Output

After successful run, **4 CSV files** will be generated in the `output/` folder:

```
output/
├── products.csv          (all products from product_details_gen2)
├── accessories.csv       (all accessories from accessories_details_gen2)
├── invoices.csv          (all invoices from invoice_gen2)
└── invoice_items.csv     (all items from invoices)
```

## ✅ Success Output Example

```
🚀 Starting Firebase to CSV Export...

✓ Firebase initialized successfully

📦 Exporting Products...
   ✓ Exported 45 products
📦 Exporting Accessories...
   ✓ Exported 28 accessories
📦 Exporting Invoices & Items...
   ✓ Exported 156 invoices
   ✓ Exported 423 invoice items

✅ Export completed successfully!
📁 Files saved to: /path/to/analytics-exporter/output
```

## 🆘 Troubleshooting

| Error | Solution |
|-------|----------|
| `FileNotFoundException: serviceAccountKey.json` | Get service account JSON from Firebase Console and place in analytics-exporter folder |
| `PERMISSION_DENIED` | Check Firestore security rules allow read access |
| `Collection not found` | Verify collection names: `product_details_gen2`, `accessories_details_gen2`, `invoice_gen2` |
| `No output files generated` | Ensure collections have data in Firestore |
| `Maven not found` | Install Maven: `choco install maven` (Windows) or `brew install maven` (Mac) |
| `java.lang.ClassNotFoundException` | Use `mvn clean compile` first, then run with `mvn exec:java` |

## 📊 CSV File Details

### products.csv
**Columns:** id, name, code, price, owner, status, documentId, dealer, attarSellingPriceMap, perfumeSellingPriceMap
- One row per product
- HashMap fields are flattened: `key1:value1|key2:value2`

### accessories.csv
**Columns:** id, name, sellingPrice, actualPrice, documentId, owner, dealer, status
- One row per accessory

### invoices.csv
**Columns:** invoiceId, billingDate, totalCost, discount, customerName, customerPhone, paymentMode, upiPaymentStatus, isPrint, isCourier, courierAmount, cardCharges, sellingCost, remarks, clientName, clientPhoneNo
- One row per invoice
- Use `invoiceId` to join with `invoice_items.csv`

### invoice_items.csv
**Columns:** invoiceId, itemType, itemName, itemCode, units, unitPrice, totalPrice, sellingItemPrice, productCategory, pieces
- One row per line item
- Linked to invoices.csv using `invoiceId`

## 🎯 Usage Tips

1. **View CSV in Excel**: Open the CSV files directly in Microsoft Excel or Google Sheets
2. **Join Invoice Data**: Use `invoiceId` in Excel to create pivot tables combining invoices and items
3. **Re-export**: Run the script again to overwrite existing CSV files
4. **Large Datasets**: The script handles large Firebase collections efficiently
5. **Data Validation**: Check for null/empty fields shown as blank cells

## 📋 CSV Specifications

- **Encoding**: UTF-8
- **Delimiter**: Comma (,)
- **Quote Character**: Double quote (")
- **Escape**: Double quote ("") for internal quotes
- **Special Characters**: Properly escaped and quoted
- **Null Values**: Empty strings
- **Timestamps**: Stored as milliseconds (ISO format if applicable)

---

**Need help?** Check the error message or review the code comments in `FirebaseCsvExporter.java`
