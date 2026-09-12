# Firebase CSV Analytics Exporter

**Export Firebase Firestore data to CSV files for analytics and customer follow-up**

## 📦 What's Inside

A standalone Java script that exports 3 Firebase collections to **4 CSV files** automatically:

| Source Collection | CSV Output | Content |
|-------------------|-----------|---------|
| `product_details_gen2` | `products.csv` | All products (flat) |
| `accessories_details_gen2` | `accessories.csv` | All accessories (flat) |
| `invoice_gen2` | `invoices.csv` | Invoice headers (flat) |
| `invoice_gen2` | `invoice_items.csv` | Line items (one per row, linked by invoiceId) |

## 🎯 Features

✅ **Single command execution** - Runs and generates all 4 CSVs automatically
✅ **Nested data handling** - Invoices with multiple items are properly split into 2 CSVs
✅ **CSV-safe escaping** - Special characters (commas, quotes, newlines) properly escaped
✅ **No UI required** - Pure command-line standalone script
✅ **Firebase Admin SDK** - Uses official Google Cloud libraries
✅ **Error handling** - Clear error messages for troubleshooting

## 📋 Files in This Folder

| File | Purpose |
|------|---------|
| `FirebaseCsvExporter.java` | Main executable class |
| `pom.xml` | Maven configuration & dependencies |
| `QUICK_START.md` | Step-by-step setup & run guide |
| `README.md` | This file |

## 🚀 Quick Start (5 minutes)

### 1. Get Firebase Credentials
- Go to Firebase Console → Project Settings → Service Accounts
- Generate New Private Key (Java)
- Save as `serviceAccountKey.json` in this folder

### 2. Run the Script
```bash
cd analytics-exporter
mvn clean compile
mvn exec:java -Dexec.mainClass="FirebaseCsvExporter"
```

### 3. Find Your CSV Files
All output files are in `output/` folder

**That's it!** 🎉

---

## 📚 Detailed Guide

See **QUICK_START.md** for:
- Detailed step-by-step instructions
- Multiple run options (Maven, Gradle, Direct Java)
- Troubleshooting guide
- CSV file structure documentation
- Usage tips and best practices

## 🔧 System Requirements

- Java 11 or higher
- Maven 3.6+ (or Gradle 7+)
- Firebase project with Firestore database
- Internet connection (to reach Firebase)

## 📊 Output Example

After running, you'll get:
```
analytics-exporter/
├── output/
│   ├── products.csv           (e.g., 45 rows)
│   ├── accessories.csv        (e.g., 28 rows)
│   ├── invoices.csv           (e.g., 156 rows)
│   └── invoice_items.csv      (e.g., 423 rows)
├── FirebaseCsvExporter.java
├── pom.xml
├── serviceAccountKey.json
└── README.md
```

## 💡 Use Cases

- 📈 **Analytics**: Import CSVs into Google Sheets/Excel for analysis
- 👥 **Customer Follow-up**: Export customer & invoice data for CRM
- 📋 **Reporting**: Generate reports combining invoices and items
- 🔄 **Data Migration**: Backup Firebase data as CSV
- 🔍 **Auditing**: Verify data integrity and completeness

## 🤝 Join Invoices & Items in Excel

Use this formula to link invoice data with items:
```
=VLOOKUP(invoiceId, invoice_items.csv!A:J, 2, FALSE)
```

Or create a pivot table in Google Sheets for advanced analysis.

## ⚠️ Important Notes

1. **Permissions**: Ensure your Firebase Firestore rules allow read access
2. **Data Size**: Script handles large datasets efficiently
3. **Timestamps**: Stored as milliseconds since epoch (UNIX time)
4. **Maps/Objects**: Flattened using pipe-colon format: `key1:value1|key2:value2`
5. **Re-export**: Running again overwrites previous output files

## 🆘 Troubleshooting

**Problem**: `FileNotFoundException: serviceAccountKey.json`
```
Solution: Download service account from Firebase Console → Place in analytics-exporter folder
```

**Problem**: `PERMISSION_DENIED` error
```
Solution: Check Firestore security rules allow read access from service account
```

**Problem**: No CSV files generated
```
Solution: Verify collections exist in Firestore: product_details_gen2, accessories_details_gen2, invoice_gen2
```

For more help, see **QUICK_START.md**

---

**Ready to export?** → Open QUICK_START.md and follow the guide! 🚀
