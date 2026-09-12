#!/bin/bash

# Firebase CSV Exporter - Linux/Mac Shell Script
# This script compiles and runs the Firebase CSV exporter

echo ""
echo "===================================================="
echo "  Firebase to CSV Analytics Exporter"
echo "===================================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "[ERROR] Maven is not installed!"
    echo ""
    echo "Install Maven from: https://maven.apache.org/download.cgi"
    echo "Or use: brew install maven (macOS) or apt-get install maven (Linux)"
    echo ""
    exit 1
fi

# Check if serviceAccountKey.json exists
if [ ! -f "serviceAccountKey.json" ]; then
    echo "[ERROR] serviceAccountKey.json not found!"
    echo ""
    echo "Please:"
    echo "1. Go to: https://console.firebase.google.com/"
    echo "2. Select project: sha-attar-invoice"
    echo "3. Settings > Service Accounts"
    echo "4. Click 'Generate New Private Key'"
    echo "5. Save the file as: serviceAccountKey.json in this folder"
    echo ""
    exit 1
fi

echo "[1/3] Cleaning and compiling..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "[ERROR] Compilation failed!"
    exit 1
fi

echo ""
echo "[2/3] Running Firebase CSV Exporter..."
mvn exec:java -Dexec.mainClass="FirebaseCsvExporter"
if [ $? -ne 0 ]; then
    echo "[ERROR] Execution failed!"
    exit 1
fi

echo ""
echo "[3/3] Done! Check output folder for CSV files"
echo ""
echo "===================================================="
echo "  CSV files generated in: output/"
echo "===================================================="
echo ""
