package com.app.sha.attar.invoice.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.InvoiceHistoryViewAdapter;
import com.app.sha.attar.invoice.listener.BillingClickListener;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.utils.BluetoothPrinterHelper;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.DatabaseConstants;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.PDFHelper;
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class InvoiceHistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_BLUETOOTH = 1;
    private static final int PERMISSION_BLUETOOTH_ADMIN = 2;
    private static final int PERMISSION_BLUETOOTH_CONNECT = 3;
    private static final int PERMISSION_BLUETOOTH_SCAN = 4;
    private static final int REQUEST_ENABLE_BT = 10;
    Context context;
    Activity activity;

    OffsetDateTime startOfDay;
    OffsetDateTime endOfDay;

    TextView back;
    TextView start_tv, end_tv;
    Button submit_bt;

    LinearLayout filter_ll;
    FrameLayout data_ll, no_data_ll;
    FloatingActionButton add_fab;

    RecyclerView data_recycler_view;

    List<BillingInvoiceModel> contentList = new ArrayList<>();
    InvoiceHistoryViewAdapter adapter;
    BillingClickListener listener;

    DBUtil dbObj;
    FirebaseFirestore db;

    boolean owner;
    SharedPrefHelper sharedPrefHelper;

    BluetoothAdapter bluetoothAdapter;
    public BluetoothPrinterHelper bluetoothPrinterHelper;

    PDFHelper pdfHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_invoice_history);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        context = InvoiceHistoryActivity.this;
        activity = InvoiceHistoryActivity.this;
        dbObj = new DBUtil();
        db = DBUtil.getInstance();
        sharedPrefHelper = new SharedPrefHelper(context);
        bluetoothPrinterHelper = new BluetoothPrinterHelper(context, activity);
        pdfHelper = new PDFHelper(context);
        back = (TextView) findViewById(R.id.invoice_history_back);
        back.setOnClickListener(this);

        start_tv = (TextView) findViewById(R.id.invoice_history_start_date_tv);
        start_tv.setOnClickListener(this);
        end_tv = (TextView) findViewById(R.id.invoice_history_end_date_tv);
        end_tv.setOnClickListener(this);

        submit_bt = (Button) findViewById(R.id.invoice_history_search);
        submit_bt.setOnClickListener(this);

        filter_ll = (LinearLayout) findViewById(R.id.invoice_history_filter_ll);
        data_ll = (FrameLayout) findViewById(R.id.invoice_history_data_ll);
        no_data_ll = (FrameLayout) findViewById(R.id.invoice_history_no_data_ll);

        add_fab = (FloatingActionButton) findViewById(R.id.invoice_history_add_fab);
        add_fab.setOnClickListener(this);

        Intent intent = getIntent();
        owner = intent.getBooleanExtra("owner", false);

        listener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {

                if (checkInternet() && type.equalsIgnoreCase("EDIT")) {
                    //callDetailActivity(context, filteredList.get(index));
                    System.out.println("clicked item : " + index);
                    Intent i = new Intent(InvoiceHistoryActivity.this, InvoiceHistoryDetailsActivity.class);
                    i.putExtra("invoiceId", String.valueOf(contentList.get(index).getBillingDate()));
                    startActivity(i);
                } else if (checkInternet() && type.equalsIgnoreCase("DELETE")) {
                    deleteConfirmationPopup(index);
                } else if (checkInternet() && type.equalsIgnoreCase("SHARE")) {
                    shareInvoiceDetails(index);
                } else if (checkInternet() && type.equalsIgnoreCase("WHATSAPP")) {
                    shareInvoiceDetailsToWhatsapp(index);
                } else if (checkInternet() && type.equalsIgnoreCase("PRINT")) {
                    if (!bluetoothAdapter.isEnabled()) {
                        Toast.makeText(context, "Please turn ON Bluetooth", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        // Get Paired Devices
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                        if (!pairedDevices.isEmpty()) {
                            boolean state = false;
                            for (BluetoothDevice device : pairedDevices) {
                                if (device.getName().contains("RP3230") && !state) {
                                    printConfirmationPopup(contentList.get(index), device.getName());
                                    state = true;
                                }
                            }
                            if (!state) {
                                Toast.makeText(context, "Printer not connected properly.!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "No paired devices found", Toast.LENGTH_SHORT).show();
                        }

                    }

                }
            }
        };

        data_recycler_view = (RecyclerView) findViewById(R.id.invoice_history_recyclerView);
        adapter = new InvoiceHistoryViewAdapter(context, contentList, listener, owner);
        data_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        data_recycler_view.setAdapter(adapter);

        if (!owner) {
            filter_ll.setVisibility(View.GONE);
            getServerDate();
        }
        enableBluetooth();

    }

    private void shareInvoiceDetailsToWhatsapp(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("WhatsApp Number");

        final EditText input = new EditText(this);
        input.setHint("e.g. 9876543210");  // With country code
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {

            if (!input.getText().toString().trim().isEmpty()) {
                String phoneNumber = "91"+input.getText().toString().trim();
                // Example invoice text

                // Get PDF file (for demo: assuming it's in internal storage)
                String fileName = pdfHelper.createPdfAndShare(contentList.get(index));
                // Share PDF
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

                if (file.exists()) {
                    sendInvoiceToWhatsApp(phoneNumber, file);
                } else {
                    Toast.makeText(this, "Invoice PDF not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();


    }


    private void sendInvoiceToWhatsApp(String phoneNumber, File pdfFile) {
        try {

            // ✅ Get URI for File using FileProvider
            Uri fileUri =FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);

            // ✅ Create Intent
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            //sendIntent.setType("*/*");  // For both text and file
            sendIntent.setType("application/pdf");
            sendIntent.setPackage("com.whatsapp");
            sendIntent.putExtra("jid", phoneNumber + "@s.whatsapp.net"); // For direct message
            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "WhatsApp not installed or error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show();
            return;
        }
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_BLUETOOTH);
                return;
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PERMISSION_BLUETOOTH_ADMIN);
                return;
            } else {
                // Your Bluetooth logic here
            }
        } else {
            // For Android 12 (S) and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_BLUETOOTH_CONNECT);
                return;
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_BLUETOOTH_SCAN);
                return;
            } else {
                // Your Bluetooth logic here
            }
        }

        // Permission granted, request to enable Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth(); // Retry enabling Bluetooth
            } else {
                Toast.makeText(this, "Bluetooth permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void shareInvoiceDetails(int index) {
        String fileName = pdfHelper.createPdfAndShare(contentList.get(index));
        // Share PDF
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, "Share receipt"));
    }

    private void printConfirmationPopup(BillingInvoiceModel billData, String printer) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation (" + printer + ")");
        if (billData.getIsPrint() == null || !billData.getIsPrint()) {
            builder.setMessage("Do you want to print the bill?");
        } else {
            builder.setMessage("Already Bill printed. Do you want to print it again?");
        }
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            try {
                if (bluetoothPrinterHelper.printSmallFontReceipt(billData))
                    updatePrintStatusToDatabase(billData);
            } catch (Exception e) {
                Toast.makeText(context, "Printer not available. Please restart the printer.!", Toast.LENGTH_LONG).show();
            }
        });

        // Set negative button
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    private void updatePrintStatusToDatabase(BillingInvoiceModel billingInvoiceModel) {
        billingInvoiceModel.setIsPrint(true);
        db.collection(DatabaseConstants.INVOICE_COLLECTION)
                .document(String.valueOf(billingInvoiceModel.getBillingDate()))
                .set(billingInvoiceModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Internal server error..!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getServerDate() {
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(context);
        OffsetDateTime offsetDateTime = null;
        if (StringUtils.isNotBlank(sharedPrefHelper.getSystemTime())) {
            offsetDateTime = OffsetDateTime.parse(sharedPrefHelper.getSystemTime()).withOffsetSameInstant(ZoneOffset.ofHoursMinutes(5, 30));
        } else {
            offsetDateTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.ofHoursMinutes(5, 30));
        }
        startOfDay = offsetDateTime.withHour(0).withMinute(0).withSecond(0).minusDays(1);
        endOfDay = offsetDateTime.withHour(23).withMinute(59).withSecond(59);
        getInvoiceRecords(startOfDay.toEpochSecond(), endOfDay.toEpochSecond());

    }

    private void deleteConfirmationPopup(int index) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to delete?");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteInvoiceDetail(index);
            dialog.dismiss();
        });

        // Set negative button
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void deleteInvoiceDetail(int index) {
        db.collection(DatabaseConstants.INVOICE_COLLECTION)
                .document(String.valueOf(contentList.get(index).getBillingDate()))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(InvoiceHistoryActivity.this, "Invoice item deleted .!", Toast.LENGTH_LONG).show();
                    contentList.remove(index);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(InvoiceHistoryActivity.this, "Invoice deleted failed..!", Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (startOfDay != null && endOfDay != null) {
            getInvoiceRecords(startOfDay.toEpochSecond(), endOfDay.toEpochSecond());
        }
    }


    private void getInvoiceRecords(long startOfDay, long endOfDay) {
        if (!owner && !"ADMIN".equalsIgnoreCase(sharedPrefHelper.getLoginUserType())) {
            dbObj.getBillingInvoiceDetail(new FirestoreCallback<List<BillingInvoiceModel>>() {
                @Override
                public void onCallback(List<BillingInvoiceModel> result) {

                    if (result.isEmpty()) {
                        Toast.makeText(InvoiceHistoryActivity.this, "No Invoice Data found .!", Toast.LENGTH_LONG).show();
                        no_data_ll.setVisibility(View.VISIBLE);
                        data_ll.setVisibility(View.GONE);
                        return;
                    }
                    contentList.clear();
                    contentList.addAll(result);
                    data_ll.setVisibility(View.VISIBLE);
                    no_data_ll.setVisibility(View.GONE);
                    Toast.makeText(InvoiceHistoryActivity.this, "Invoice Data Loaded .!", Toast.LENGTH_LONG).show();

                    adapter.notifyDataSetChanged();

                }
            }, startOfDay, endOfDay, sharedPrefHelper.getLoginUserPhone());
        } else {
            dbObj.getBillingInvoiceDetail(new FirestoreCallback<List<BillingInvoiceModel>>() {
                @Override
                public void onCallback(List<BillingInvoiceModel> result) {

                    if (result.isEmpty()) {
                        Toast.makeText(InvoiceHistoryActivity.this, "No Invoice Data found .!", Toast.LENGTH_LONG).show();
                        no_data_ll.setVisibility(View.VISIBLE);
                        data_ll.setVisibility(View.GONE);
                        return;
                    }
                    contentList.clear();
                    contentList.addAll(result);
                    data_ll.setVisibility(View.VISIBLE);
                    no_data_ll.setVisibility(View.GONE);
                    Toast.makeText(InvoiceHistoryActivity.this, "Invoice Data Loaded .!", Toast.LENGTH_LONG).show();

                    adapter.notifyDataSetChanged();

                }
            }, startOfDay, endOfDay);
        }

    }

    @Override
    public void onClick(View view) {
        if (R.id.invoice_history_back == view.getId()) {
            finish();
        } else if (R.id.invoice_history_start_date_tv == view.getId()) {
            showStartDatePickerDialog();
        } else if (R.id.invoice_history_end_date_tv == view.getId()) {
            showEndDatePickerDialog();
        } else if (R.id.invoice_history_search == view.getId()) {
            if (startOfDay == null) {
                Toast.makeText(context, "Select Start date ..!", Toast.LENGTH_LONG).show();
                return;
            }
            if (endOfDay == null) {
                Toast.makeText(context, "Select End date ..!", Toast.LENGTH_LONG).show();
                return;
            }
            if (checkInternet())
                getInvoiceRecords(startOfDay.toEpochSecond(), endOfDay.toEpochSecond());
        } else if (R.id.invoice_history_add_fab == view.getId()) {
            //call intent to  invoice detail activity
            Intent i = new Intent(InvoiceHistoryActivity.this, InvoiceHistoryDetailsActivity.class);
            i.putExtra("owner", owner);
            startActivity(i);
        }
    }

    private boolean checkInternet() {
        if (SingleTon.isNetworkConnected(activity)) {
            return true;
        } else {
            Toast.makeText(context, "No Internet connection. Please try again .! ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    private void showStartDatePickerDialog() {

        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                InvoiceHistoryActivity.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Update the TextView with the selected date
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    start_tv.setText(dateFormat.format(calendar.getTime()));
                    LocalDate localDate = calendar.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    // Create OffsetDateTime with 00:00 time
                    startOfDay = localDate.atTime(LocalTime.MIN).atZone(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
                },
                year,
                month,
                day
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();

    }

    private void showEndDatePickerDialog() {

        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                InvoiceHistoryActivity.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Update the TextView with the selected date
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    end_tv.setText(dateFormat.format(calendar.getTime()));
                    LocalDate localDate = calendar.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    endOfDay = localDate.atTime(LocalTime.MAX).atZone(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
                },
                year,
                month,
                day
        );
        datePickerDialog.getDatePicker().setMinDate((startOfDay != null) ? startOfDay.toInstant().toEpochMilli() : System.currentTimeMillis());
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth enabled successfully
            } else {
                // User denied to enable Bluetooth
            }
        }
    }

}