package com.app.sha.attar.invoice.activity;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.BillingViewAdapter;
import com.app.sha.attar.invoice.adapter.CartItemAdapter;
import com.app.sha.attar.invoice.listener.BillingClickListener;
import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.ConfigModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.utils.BluetoothPrinterHelper;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.DatabaseConstants;
import com.app.sha.attar.invoice.utils.PDFHelper;
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    DrawerLayout mDrawerLayout;
    NavigationView navigationView;

    List<BillingItemModel> billingItemModelList = new ArrayList<>();

    List<ProductModel> productModelList = new ArrayList<>();
    List<AccessoriesModel> accessoriesModelList = new ArrayList<>();

    LinearLayout content_ll,empty_ll;

    RecyclerView bill_recycler;
    BillingViewAdapter billingAdapter;
    BillingClickListener listener;

    Context context;
    Activity activity;

    Button billing_add, billing_button, complete_order_button;
    TextView billing_total_amount, billing_selling_amount, billing_discount;

    LinearLayout billing_discount_ll;

    TextView finalBillingAmountTv;

    CheckBox courier_checkBox;
    EditText courier_amount;

    RadioGroup paymentGroup;
    RadioButton cashRadioBt, upiRadioBt;

    Double totalAmount = 0.0, sellingAmount = 0.0, discount = 0.0, cardChargeAmount = 0.0;

    // Cart dialog components
    Dialog cartDialog;

    SharedPrefHelper sharedPrefHelper;
    DBUtil dbObj;
    FirebaseFirestore db;

    private static final int REQUEST_WRITE_PERMISSION = 786;
    private static final int REQUEST_ENABLE_BT = 10;
    private static final int PERMISSION_BLUETOOTH = 1;
    private static final int PERMISSION_BLUETOOTH_ADMIN = 2;
    private static final int PERMISSION_BLUETOOTH_CONNECT = 3;
    private static final int PERMISSION_BLUETOOTH_SCAN = 4;

    String paymentMode = "";

    PDFHelper pdfHelper;

    BluetoothAdapter bluetoothAdapter;
    public BluetoothPrinterHelper bluetoothPrinterHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_main);

        context = MainActivity.this;
        activity = MainActivity.this;

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ImageView textView = (ImageView) findViewById(R.id.home_nav_text_view);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ("ADMIN".equalsIgnoreCase(sharedPrefHelper.getLoginUserType())) {
                    if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    } else {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    }
                } else {
                    Toast.makeText(context, "You are not a Admin .! ", Toast.LENGTH_LONG).show();
                }
            }
        });
        dbObj = new DBUtil();
        db = DBUtil.getInstance();
        pdfHelper = new PDFHelper(context);
        sharedPrefHelper = new SharedPrefHelper(context);
        bluetoothPrinterHelper = new BluetoothPrinterHelper(context, activity);
        sharedPrefHelper.setTotalProductItem();
        sharedPrefHelper.setTotalAccessoriesItem();
        productModelList.addAll(sharedPrefHelper.getTotalProductList());
        accessoriesModelList.addAll(sharedPrefHelper.getTotalAccessoriesList());

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        content_ll = (LinearLayout) findViewById(R.id.home_content_ll);
        empty_ll = (LinearLayout) findViewById(R.id.home_empty_ll);

        billing_add = (Button) findViewById(R.id.home_bill_add_bt);
        billing_add.setOnClickListener(this);

        complete_order_button = (Button) findViewById(R.id.complete_order_button);
        complete_order_button.setOnClickListener(this);

//        billing_total_amount = (TextView) findViewById(R.id.billing_total_amount_price);
//        billing_selling_amount = (TextView) findViewById(R.id.billing_total_selling_price);
//        billing_discount = (TextView) findViewById(R.id.billing_discount);
//        billing_discount_ll = (LinearLayout) findViewById(R.id.billing_discount_ll);
//        billing_button = (Button) findViewById(R.id.billing_submit_invoice);
//
//        paymentGroup = (RadioGroup) findViewById(R.id.new_bill_payment_radio_group);
//        cashRadioBt = (RadioButton) findViewById(R.id.new_billing_payment_cash);
//        upiRadioBt = (RadioButton) findViewById(R.id.new_billing_payment_upi);
//
//        courier_checkBox = findViewById(R.id.new_billing_is_courier_checkBox);
//        courier_amount = findViewById(R.id.new_billing_courier_amount);
//        finalBillingAmountTv = findViewById(R.id.tvFinalPayableAmount);

        // Initialize cart dialog components as null (will be set when dialog opens)
//        billing_total_amount = null;
//        billing_selling_amount = null;
//        billing_discount = null;
//        billing_discount_ll = null;
//        billing_button = null;
//        paymentGroup = null;
//        cashRadioBt = null;
//        upiRadioBt = null;
//        courier_checkBox = null;
//        courier_amount = null;
//        finalBillingAmountTv = null;
//
//        cashRadioBt.setChecked(true);
//        paymentMode = "CASH";
//        paymentGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                // Find which radio button is selected
//                if (R.id.new_billing_payment_cash == checkedId) {
//                    paymentMode = "CASH";
//                    onCardChargeClicked(sellingAmount, false);
//                } else if (R.id.new_billing_payment_upi == checkedId) {
//                    paymentMode = "UPI";
//                    onCardChargeClicked(sellingAmount, false);
//                } else if (R.id.new_billing_payment_card == checkedId) {
//                    paymentMode = "CARD";
//                    onCardChargeClicked(sellingAmount, true);
//                }
//            }
//        });

        FloatingActionButton allClear = (FloatingActionButton) findViewById(R.id.new_billing_all_clear);
//        billing_discount_ll.setOnClickListener(this);
//        billing_button.setOnClickListener(this);

//        courier_checkBox = findViewById(R.id.new_billing_is_courier_checkBox);
//        courier_amount = findViewById(R.id.new_billing_courier_amount);
//        finalBillingAmountTv = findViewById(R.id.tvFinalPayableAmount);

        allClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billingItemModelList.clear();
                billingAdapter.notifyDataSetChanged();
                manageBillingLayout();
            }
        });

//        courier_checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                // Expand and show EditText
//                courier_amount.setVisibility(View.VISIBLE);
//                upiRadioBt.setChecked(true);
//                onCardChargeClicked(sellingAmount,false);
//            } else {
//                // Hide EditText
//                courier_amount.setText("0.0");
//                courier_amount.setVisibility(View.GONE);
//            }
//        });

        bill_recycler = (RecyclerView) findViewById(R.id.home_recyclerView);

        ImageView home_invoice_tv = (ImageView) findViewById(R.id.home_invoice_history);

        home_invoice_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getServerDate();
            }
        });

        listener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if ("REMOVE".equalsIgnoreCase(type)) {
                    billingItemModelList.remove(index);
                    billingAdapter.notifyDataSetChanged();
                    manageBillingLayout();
                } else if ("UPDATE".equalsIgnoreCase(type)) {
                    createNewBillDialog(context, billingItemModelList.get(index));
                }
            }
        };


        billingAdapter = new BillingViewAdapter(context, billingItemModelList, listener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setStackFromEnd(true);
        bill_recycler.setLayoutManager(layoutManager);
        bill_recycler.setAdapter(billingAdapter);

        billingAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                //bill_recycler.scrollToPosition((billingItemModelList.size()>0)?billingItemModelList.size() - 1:0);
                if (billingAdapter.getItemCount() > 0)
                    bill_recycler.post(() -> bill_recycler.scrollToPosition(billingAdapter.getItemCount() - 1));
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            // Request the necessary permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO},
                    REQUEST_WRITE_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_PERMISSION);
        }
        enableBluetooth();
        manageBillingLayout();

    }


    private void showCartDialog() {
        cartDialog = new Dialog(context);
        cartDialog.setContentView(R.layout.dialog_cart_summary);
        cartDialog.setCanceledOnTouchOutside(false);
        cartDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cartDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        cartDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        cartDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize cart dialog components
        billing_total_amount = cartDialog.findViewById(R.id.billing_total_amount_price);
        billing_selling_amount = cartDialog.findViewById(R.id.billing_total_selling_price);
        billing_discount = cartDialog.findViewById(R.id.billing_discount);
        billing_discount_ll = cartDialog.findViewById(R.id.billing_discount_ll);
        billing_button = cartDialog.findViewById(R.id.billing_submit_invoice);
        paymentGroup = cartDialog.findViewById(R.id.new_bill_payment_radio_group);
        cashRadioBt = cartDialog.findViewById(R.id.new_billing_payment_cash);
        upiRadioBt = cartDialog.findViewById(R.id.new_billing_payment_upi);
        courier_checkBox = cartDialog.findViewById(R.id.new_billing_is_courier_checkBox);
        courier_amount = cartDialog.findViewById(R.id.new_billing_courier_amount);
        finalBillingAmountTv = cartDialog.findViewById(R.id.tvFinalPayableAmount);

        // Set up cart items recycler
        RecyclerView cartItemsRecycler = cartDialog.findViewById(R.id.cart_items_recycler);
        CartItemAdapter cartAdapter = new CartItemAdapter(context, billingItemModelList);
        LinearLayoutManager cartLayoutManager = new LinearLayoutManager(context);
        cartItemsRecycler.setLayoutManager(cartLayoutManager);
        cartItemsRecycler.setAdapter(cartAdapter);
        
        // Set up payment options
        cashRadioBt.setChecked(true);
        paymentMode = "CASH";
        paymentGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.new_billing_payment_cash == checkedId) {
                    paymentMode = "CASH";
                    onCardChargeClicked(sellingAmount, false);
                } else if (R.id.new_billing_payment_upi == checkedId) {
                    paymentMode = "UPI";
                    onCardChargeClicked(sellingAmount, false);
                } else if (R.id.new_billing_payment_card == checkedId) {
                    paymentMode = "CARD";
                    onCardChargeClicked(sellingAmount, true);
                }
            }
        });

        // Set up courier checkbox
        courier_checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                courier_amount.setVisibility(View.VISIBLE);
                upiRadioBt.setChecked(true);
                onCardChargeClicked(sellingAmount, false);
            } else {
                courier_amount.setText("0.0");
                courier_amount.setVisibility(View.GONE);
            }
        });

        // Set up click listeners
        billing_discount_ll.setOnClickListener(this);
        billing_button.setOnClickListener(this);
        
        ImageView closeButton = cartDialog.findViewById(R.id.cart_close_button);
        closeButton.setOnClickListener(v -> cartDialog.dismiss());

        // Update cart dialog with current data
        updateCartDialog();
        
        cartDialog.show();
    }

    private void updateCartDialog() {
        if (billing_total_amount != null) {
            billing_total_amount.setText("Rs. " + totalAmount);
            billing_discount.setText(discount + "%");
            billing_selling_amount.setText("Rs. " + sellingAmount);
            if (paymentMode.equalsIgnoreCase("CARD")) {
                onCardChargeClicked(sellingAmount, true);
            }
            finalBillingAmountTv.setText("Rs. " + (sellingAmount + cardChargeAmount));
        }
    }

    private void onCardChargeClicked(double billAmount, boolean isCardChargeVisible) {
        if (cartDialog == null) return;
        
        CardView cardView = cartDialog.findViewById(R.id.cardChargeCard);
        if (cardView == null) return;

        if (isCardChargeVisible) {
            TextView tvCardCharge = cartDialog.findViewById(R.id.tvCardCharge);
            TextView tvGST = cartDialog.findViewById(R.id.tvGST);
            TextView tvTotalExtra = cartDialog.findViewById(R.id.tvTotalExtra);

            double cardCharge = billAmount * 0.03;
            double gst = cardCharge * 0.18;
            double totalExtra = cardCharge + gst;

            cardCharge = round(cardCharge);
            gst = round(gst);
            totalExtra = round(totalExtra);
            cardChargeAmount = totalExtra;
            
            tvCardCharge.setText("Card Charges (3%): ₹" + cardCharge);
            tvGST.setText("GST (18%): ₹" + gst);
            tvTotalExtra.setText("Total Extra: ₹" + totalExtra);
            cardView.setVisibility(View.VISIBLE);
        } else {
            cardChargeAmount = 0.0;
            cardView.setVisibility(View.GONE);
        }
        
        if (finalBillingAmountTv != null) {
            finalBillingAmountTv.setText("Rs. " + (sellingAmount + cardChargeAmount));
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }


    private void enableBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            //Toast.makeText(this, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show();
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

    private boolean checkInternet() {
        if (SingleTon.isNetworkConnected(activity)) {
            return true;
        } else {
            Toast.makeText(context, "No Internet connection. Please try again .! ", Toast.LENGTH_LONG).show();
            return false;
        }

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

    private void manageBillingLayout() {
        if (billingItemModelList.isEmpty()) {
            complete_order_button.setVisibility(View.GONE);
            empty_ll.setVisibility(View.VISIBLE);
            content_ll.setVisibility(View.GONE);
            totalAmount = 0.0;
            discount = 0.0;
            cardChargeAmount = 0.0;
            return;
        } else {
            complete_order_button.setVisibility(View.VISIBLE);
            empty_ll.setVisibility(View.GONE);
            content_ll.setVisibility(View.VISIBLE);
            totalAmount = 0.0;
            for (BillingItemModel billingItemModel : billingItemModelList) {
                totalAmount += billingItemModel.getSellingItemPrice();
            }
        }
        sellingAmount = totalAmount - ((totalAmount * discount) / 100);
        billingAdapter.notifyDataSetChanged();
        if (billingAdapter.getItemCount() > 0)
            bill_recycler.post(() -> bill_recycler.scrollToPosition(billingAdapter.getItemCount() - 1));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent i = null;
        mDrawerLayout.closeDrawer(GravityCompat.START);
        if (item.getItemId() == R.id.nav_products) {
            i = new Intent(MainActivity.this, ProductActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_accessories) {
            i = new Intent(MainActivity.this, AccessoriesActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_packaging) {
            i = new Intent(MainActivity.this, PackageActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_report) {
            i = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_sales_person) {
            i = new Intent(MainActivity.this, SalesPersonActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_invoice) {
            i = new Intent(MainActivity.this, InvoiceHistoryActivity.class);
            i.putExtra("owner", true);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_consolidate_report) {
            i = new Intent(MainActivity.this, ConsolidateReportActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_sales_person_history) {
            i = new Intent(MainActivity.this, CustomerHistoryActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_sales_person_report) {
            i = new Intent(MainActivity.this, SalesPersonReportActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_asset_business_calc) {
            i = new Intent(MainActivity.this, AssetBusinessCalcActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_expense_tracker) {
            i = new Intent(MainActivity.this, ExpenseTrackerActivity.class);
            startActivity(i);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPrefHelper.setTotalProductItem();
        sharedPrefHelper.setTotalAccessoriesItem();
        productModelList.clear();
        accessoriesModelList.clear();
        productModelList.addAll(sharedPrefHelper.getTotalProductList());
        accessoriesModelList.addAll(sharedPrefHelper.getTotalAccessoriesList());
    }

    @Override
    public void onClick(View view) {
        if (R.id.home_bill_add_bt == view.getId()) {
            if (checkInternet())
                createNewBillDialog(context, null);
        } else if (R.id.complete_order_button == view.getId()) {
            showCartDialog();
        } else if (R.id.billing_discount_ll == view.getId()) {
            createDiscountDialog();
        } else if (R.id.billing_submit_invoice == view.getId()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (checkInternet())
                submitInvoiceDetails();
        }
    }


    private void submitInvoiceDetails() {

        if (billingItemModelList == null || billingItemModelList.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please Add products / Accessories..!", Toast.LENGTH_LONG).show();
            return;
        }

        if (StringUtils.isEmpty(paymentMode)) {
            Toast.makeText(MainActivity.this, "Please Choose Payment mode..!", Toast.LENGTH_LONG).show();
            return;
        }

        if (courier_checkBox.isChecked() && StringUtils.isEmpty(courier_amount.getText().toString())) {
            Toast.makeText(MainActivity.this, "Please Enter Courier Amount..!", Toast.LENGTH_LONG).show();
            return;
        }


        BillingInvoiceModel billingInvoiceModel = new BillingInvoiceModel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            billingInvoiceModel.setBillingDate(OffsetDateTime.now().toEpochSecond());
        }

        billingInvoiceModel.setCustomerName(sharedPrefHelper.getLoginUserName());
        billingInvoiceModel.setCustomerPhone(sharedPrefHelper.getLoginUserPhone());
        billingInvoiceModel.setPaymentMode(paymentMode);
        billingInvoiceModel.setDiscount(discount);
        billingInvoiceModel.setSellingCost(sellingAmount);
        billingInvoiceModel.setTotalCost(totalAmount);
        billingInvoiceModel.setIsPrint(false);
        billingInvoiceModel.setIsCourier(courier_checkBox.isChecked());
        billingInvoiceModel.setCardCharges(cardChargeAmount);
        billingInvoiceModel.setCourierAmount(StringUtils.isNotBlank(courier_amount.getText().toString()) ? Double.parseDouble(courier_amount.getText().toString()) : 0.0);
        for (BillingItemModel item : billingItemModelList) {
            item.setInvoiceId(billingInvoiceModel.getBillingDate());
        }
        billingInvoiceModel.setBillingItemModelList(billingItemModelList);
        if (billingInvoiceModel.getBillingItemModelList().isEmpty()) {
            Toast.makeText(MainActivity.this, "Please Add products / Accessories..!", Toast.LENGTH_LONG).show();
            return;
        }
        cartDialog.dismiss();
        if (!billingInvoiceModel.getIsCourier() && paymentMode.equalsIgnoreCase("UPI")) {
            generatePaymentQR(billingInvoiceModel);
        } else {
            if (billingInvoiceModel.getIsCourier() && paymentMode.equalsIgnoreCase("UPI")) {
                billingInvoiceModel.setUpiPaymentStatus("SUCCESS");
            }
            registerDatabase(billingInvoiceModel);
        }

    }

    private void registerDatabase(BillingInvoiceModel billingInvoiceModel) {
        Toast.makeText(MainActivity.this, "Loading..!", Toast.LENGTH_LONG).show();
        db.collection(DatabaseConstants.INVOICE_COLLECTION)
                .document(String.valueOf(billingInvoiceModel.getBillingDate()))
                .set(billingInvoiceModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainActivity.this, "Submitted Successfully..!", Toast.LENGTH_LONG).show();
                        sharePrintWhatsappDialog(billingInvoiceModel);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Internal server error..!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void generatePaymentQR(BillingInvoiceModel billingInvoiceModel) {

        Double totalPay = (billingInvoiceModel.getIsCourier()) ? billingInvoiceModel.getSellingCost() + billingInvoiceModel.getCourierAmount() : billingInvoiceModel.getSellingCost();

        String upiUri = "upi://pay?pa=" + sharedPrefHelper.getUpiId() +
                "&pn=" + sharedPrefHelper.getPayeeName() +
                "&am=" + String.format("%.2f", totalPay) +
                "&cu=INR" +
                "&tn=" + "SHA'S ATTAR & PERFUMES";

        Bitmap qrBitmap = generateQRCode(upiUri);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_qr_payment, null);

        ImageView close = view.findViewById(R.id.main_bill_qr_image_close);
        TextView payeeName = view.findViewById(R.id.main_bill_qr_payee_name);
        ImageView qrImage = view.findViewById(R.id.main_bill_qr_image);
        Button btnPayWithUPI = view.findViewById(R.id.btnPayWithUPI);
        Button btnManualSubmit = view.findViewById(R.id.main_bill_qr_image_submit);
        payeeName.setText(sharedPrefHelper.getPayeeName() + "(" + sharedPrefHelper.getUpiId() + ")");
        qrImage.setImageBitmap(qrBitmap);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnPayWithUPI.setOnClickListener(v -> {
            //startUPIIntent(upiUri);
        });

        btnManualSubmit.setOnClickListener(v -> {
            Toast.makeText(context, "Payment Acknowledged", Toast.LENGTH_SHORT).show();
            billingInvoiceModel.setUpiPaymentStatus("SUCCESS");
            registerDatabase(billingInvoiceModel);
            dialog.dismiss();
        });

        close.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "UPI Payment Still Pending.!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    private Bitmap generateQRCode(String upiUri) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(upiUri, BarcodeFormat.QR_CODE, 500, 500);
            return new BarcodeEncoder().createBitmap(bitMatrix);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sharePrintWhatsappDialog(BillingInvoiceModel billingInvoiceModel) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_main_bill_share);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroupOptions);
        RadioButton radioPrinter = dialog.findViewById(R.id.radioPrinter);
        RadioButton radioWhatsapp = dialog.findViewById(R.id.radioWhatsapp);
        EditText editWhatsappNumber = dialog.findViewById(R.id.editWhatsappNumber);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);
        ImageView btnClose = dialog.findViewById(R.id.bill_share_close);
        if(billingInvoiceModel.getIsCourier()){
            radioWhatsapp.setChecked(true);
            editWhatsappNumber.setVisibility(View.VISIBLE);
        }else {
            radioPrinter.setChecked(true);
            editWhatsappNumber.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                billingItemModelList.clear();
                manageBillingLayout();
            }
        });
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioWhatsapp) {
                editWhatsappNumber.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioPrinter) {
                editWhatsappNumber.setVisibility(View.GONE);
            }
        });

        btnSubmit.setOnClickListener(v -> {

            if (radioPrinter.isChecked()) {
                if (bluetoothAdapter == null) {
                    Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(context, "Please turn ON Bluetooth", Toast.LENGTH_SHORT).show();
                    return;
                } else {
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
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                    if (!pairedDevices.isEmpty()) {
                        boolean state = false;
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getName().contains("RP3230") && !state) {
                                printConfirmationPopup(billingInvoiceModel, device.getName());
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
            } else if (radioWhatsapp.isChecked()) {
                String number = editWhatsappNumber.getText().toString().trim();
                if (number.length() != 10) {
                    editWhatsappNumber.setError("Enter WhatsApp number");
                    return;
                }
                String phoneNumber = "91" + editWhatsappNumber.getText().toString().trim();
                // Example invoice text

                // Get PDF file (for demo: assuming it's in internal storage)
                String fileName = pdfHelper.createPdfAndShare(billingInvoiceModel);

                String PaymentQRFileName = generateQRImage(billingInvoiceModel);
                // Share PDF
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                File paymentQRfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), PaymentQRFileName);
                if (file.exists()) {
                    showWhatsappChoiceDialog(phoneNumber, file,paymentQRfile);
                    billingItemModelList.clear();
                    manageBillingLayout();
                } else {
                    Toast.makeText(this, "Invoice PDF not found", Toast.LENGTH_SHORT).show();
                }

            }
            dialog.dismiss();
        });

        dialog.show();

    }

    private String generateQRImage(BillingInvoiceModel billingInvoiceModel) {
        try {

            Double totalPay = (billingInvoiceModel.getIsCourier()) ? billingInvoiceModel.getSellingCost() + billingInvoiceModel.getCourierAmount() : billingInvoiceModel.getSellingCost();

            String upiUri = "upi://pay?pa=" + sharedPrefHelper.getUpiId() +
                    "&pn=" + sharedPrefHelper.getPayeeName() +
                    "&am=" + String.format("%.2f", totalPay) +
                    "&cu=INR" +
                    "&tn=" + "SHA'S ATTAR & PERFUMES";

            Bitmap qrImage = generateQRWithShopName(upiUri,sharedPrefHelper.getPayeeName()+"("+sharedPrefHelper.getUpiId()+")");

            // Save QR bitmap to cache
            String fileName = "payment-qr-" + billingInvoiceModel.getBillingDate() + "-" + OffsetDateTime.now().toEpochSecond() + ".png";
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            FileOutputStream stream = new FileOutputStream(file);
            qrImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap generateQRWithShopName(String upiUri, String shopName) {
        try {
            // Generate QR code bitmap
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    upiUri, BarcodeFormat.QR_CODE, 500, 500, null);

            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap qrBitmap = encoder.createBitmap(bitMatrix);

            // Add shop name below QR
            Bitmap combinedBitmap = Bitmap.createBitmap(500, 600, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(combinedBitmap);

            // Draw QR code
            canvas.drawBitmap(qrBitmap, 0, 0, null);

            // Draw Shop name text
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(14);
            canvas.drawText(shopName, 250, 560, paint);

            return combinedBitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

            Toast.makeText(context, "Printing.!", Toast.LENGTH_LONG).show();
            try {
                updatePrintStatusToDatabase(billData);
                bluetoothPrinterHelper.printSmallFontReceipt(billData);
                Toast.makeText(context, "Refreshing.!", Toast.LENGTH_LONG).show();
                billingItemModelList.clear();
                manageBillingLayout();

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
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Internal server error..!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public AlertDialog showPrintingDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false); // prevent closing manually

        // Inflate custom layout (optional)
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setGravity(Gravity.CENTER_VERTICAL);

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        layout.addView(progressBar);

        TextView message = new TextView(context);
        message.setText("Printing in-progress...\nPlease wait");
        message.setTextSize(16);
        message.setPadding(30, 0, 0, 0);
        layout.addView(message);

        builder.setView(layout);

        return builder.create();

    }


    private void sendInvoiceToWhatsApp(String phoneNumber, File pdfFile,File paymentQRfile, String packageName) {
        try {

            // ✅ Get URIs for both files
            ArrayList<Uri> uris = new ArrayList<>();
            uris.add(FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile));
            uris.add(FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", paymentQRfile));

            // ✅ Create Intent for multiple files
            Intent sendIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            sendIntent.setType("*/*"); // allow any type
            sendIntent.setPackage(packageName);
            sendIntent.putExtra("jid", phoneNumber + "@s.whatsapp.net"); // direct message

            // ✅ Attach files
            sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(sendIntent);



//            // ✅ Get URI for File using FileProvider
//            Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
//
//            // ✅ Create Intent
//            Intent sendIntent = new Intent(Intent.ACTION_SEND);
//            //sendIntent.setType("*/*");  // For both text and file
//            sendIntent.setType(contentType);
//            sendIntent.setPackage(packageName);
//            sendIntent.putExtra("jid", phoneNumber + "@s.whatsapp.net"); // For direct message
//            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
//            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            startActivity(sendIntent);


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "WhatsApp not installed or error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private void showWhatsappChoiceDialog(String phoneNumber, File file,File paymentQRfile) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_whatsapp_choice);
        dialog.setCancelable(true);

        Button btnWhatsapp = dialog.findViewById(R.id.btnWhatsapp);
        Button btnWhatsappBusiness = dialog.findViewById(R.id.btnWhatsappBusiness);

        btnWhatsapp.setOnClickListener(v -> {
            sendInvoiceToWhatsApp(phoneNumber, file,paymentQRfile, "com.whatsapp");
            dialog.dismiss();
        });

        btnWhatsappBusiness.setOnClickListener(v -> {
            sendInvoiceToWhatsApp(phoneNumber, file,paymentQRfile, "com.whatsapp.w4b");
            dialog.dismiss();
        });

        dialog.show();
    }


    private void createDiscountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Discount %");

        // Set up the input
        final EditText input = new EditText(this);
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        input.setText(String.valueOf(discount));

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString();
                if (StringUtils.isEmpty(inputText)) {
                    dialog.cancel();
                    return;
                }
                discount = Double.parseDouble(inputText);
                billing_discount.setText(inputText);
                manageBillingLayout();
                dialog.cancel();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNewBillDialog(Context context, BillingItemModel billingItemModel) {

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_billing_create);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setGravity(Gravity.TOP);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ProductModel[] selectedProduct = new ProductModel[1];
        AccessoriesModel[] selectedNonProduct = new AccessoriesModel[1];

        LinearLayout product_ll = dialog.findViewById(R.id.new_bill_product_ll);
        LinearLayout non_product_ll = dialog.findViewById(R.id.new_bill_non_product_ll);
        LinearLayout product_detail_ll = dialog.findViewById(R.id.new_bill_detail_ll);

        TextView new_bill_owner = (TextView) dialog.findViewById(R.id.new_bill_item_owner);
        TextView product_selling_cost = (TextView) dialog.findViewById(R.id.new_bill_item_selling_price);


        RadioGroup typeRadioGroup = (RadioGroup) dialog.findViewById(R.id.new_bill_radio_group);
        final String[] type = {"PRODUCT"};
        RadioButton productRadioButton = (RadioButton) dialog.findViewById(R.id.new_bill_product);
        RadioButton nonProductRadioButton = (RadioButton) dialog.findViewById(R.id.new_bill_non_product);
        productRadioButton.setChecked(true);
        product_ll.setVisibility(View.VISIBLE);
        product_detail_ll.setVisibility(View.GONE);
        non_product_ll.setVisibility(View.GONE);
        typeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Find which radio button is selected
                if (R.id.new_bill_product == checkedId) {
                    product_ll.setVisibility(View.VISIBLE);
                    non_product_ll.setVisibility(View.GONE);
                    type[0] = "PRODUCT";
                } else if (R.id.new_bill_non_product == checkedId) {
                    product_ll.setVisibility(View.GONE);
                    non_product_ll.setVisibility(View.VISIBLE);
                    type[0] = "NON_PRODUCT";
                }
            }
        });

        RadioGroup productCategoryRadioGroup = (RadioGroup) dialog.findViewById(R.id.new_bill_product_split_radio_group);
        final String[] productCategoryType = {"ATTAR"};
        RadioButton productCategoryAttarRadioButton = (RadioButton) dialog.findViewById(R.id.new_bill_product_attar);
        RadioButton productCategorySprayRadioButton = (RadioButton) dialog.findViewById(R.id.new_bill_product_spray);
        productCategoryAttarRadioButton.setChecked(true);

        Spinner productQtySpinner = (Spinner) dialog.findViewById(R.id.new_bill_item_size);

        List<String> attarQtyList = Arrays.asList(
                getString(R.string.ML_6),
                getString(R.string.ML_3),
                getString(R.string.ML_12),
                getString(R.string.ML_24)
        );

        ArrayAdapter<String> attarAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                attarQtyList
        );
        attarAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productQtySpinner.setAdapter(attarAdapter);
        productQtySpinner.setSelection(0);
        List<String> sprayQtyList = Arrays.asList(
                getString(R.string.ML_10),
                getString(R.string.ML_30),
                getString(R.string.ML_50),
                getString(R.string.ML_100)
        );

        ArrayAdapter<String> sprayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                sprayQtyList
        );
        sprayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        productCategoryRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Find which radio button is selected
                if (R.id.new_bill_product_attar == checkedId) {
                    productCategoryType[0] = "ATTAR";
                    productQtySpinner.setAdapter(attarAdapter);
                } else if (R.id.new_bill_product_spray == checkedId) {
                    productCategoryType[0] = "SPRAY";
                    productQtySpinner.setAdapter(sprayAdapter);
                }
            }
        });

        String[] productQtyValue = {"0"};
        productQtySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedValue = parent.getItemAtPosition(position).toString();
                if (selectedProduct[0] != null) {
                    if (productCategoryType[0].equalsIgnoreCase("ATTAR")) {
                        product_selling_cost.setText("" + selectedProduct[0].getAttarSellingPriceMap().get(selectedValue));
                        productQtyValue[0] = selectedValue;
                    } else if (productCategoryType[0].equalsIgnoreCase("SPRAY")) {
                        product_selling_cost.setText("" + selectedProduct[0].getPerfumeSellingPriceMap().get(selectedValue));
                        productQtyValue[0] = selectedValue;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case when nothing is selected if needed
            }
        });


        AutoCompleteTextView non_product_name = (AutoCompleteTextView) dialog.findViewById(R.id.new_bill_non_product_name);
        TextView non_product_price = (TextView) dialog.findViewById(R.id.new_bill_non_product_selling_price);

        List<String> accessories_items = accessoriesModelList.stream()
                .filter(product -> "Y".equals(product.getStatus()))
                .map(AccessoriesModel::getName)
                .collect(Collectors.toList());

        // Create an ArrayAdapter to bind the items to the AutoCompleteTextView
        ArrayAdapter<String> accessories_adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, accessories_items);
        non_product_name.setAdapter(accessories_adapter);

        non_product_name.setThreshold(1);

        // Show the dropdown when the AutoCompleteTextView is focused or clicked
        non_product_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                non_product_name.showDropDown();
            }
        });

        non_product_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    non_product_name.showDropDown();
                }
            }
        });
        non_product_name.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(i);
                Optional<AccessoriesModel> resultModel = accessoriesModelList.stream()
                        .filter(model -> selectedItem.equals(model.getName()))
                        .findFirst();

                if (resultModel.isPresent()) {
                    AccessoriesModel selectNonProductModel = resultModel.get();
                    non_product_name.setText(selectNonProductModel.getName());
                    non_product_price.setText(String.valueOf(selectNonProductModel.getSellingPrice()));
                    selectedNonProduct[0] = selectNonProductModel;

                }

            }
        });

        AutoCompleteTextView product_name = (AutoCompleteTextView) dialog.findViewById(R.id.new_bill_name);

        List<String> items = productModelList.stream()
                .filter(product -> "Y".equals(product.getStatus()))
                .map(ProductModel::getName)
                .collect(Collectors.toList());

        System.out.println("data --" + items);
        // Create an ArrayAdapter to bind the items to the AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items);
        product_name.setAdapter(adapter);
        product_name.setThreshold(1);

        // Show the dropdown when the AutoCompleteTextView is focused or clicked
        product_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product_name.showDropDown();
            }
        });

        product_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    product_name.showDropDown();
                }
            }
        });
        product_name.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(i);
                Optional<ProductModel> resultModel = productModelList.stream()
                        .filter(model -> selectedItem.equals(model.getName()))
                        .findFirst();

                if (resultModel.isPresent()) {
                    ProductModel selectProductModel = resultModel.get();
                    product_name.setText(selectProductModel.getName());
                    new_bill_owner.setText(selectProductModel.getOwner());
                    selectedProduct[0] = selectProductModel;
                    if (productCategoryType[0].equalsIgnoreCase("ATTAR")) {
                        product_selling_cost.setText("" + selectedProduct[0].getAttarSellingPriceMap().get(productQtyValue[0]));
                    }else{
                        product_selling_cost.setText("" + selectedProduct[0].getPerfumeSellingPriceMap().get(productQtyValue[0]));
                    }
                    product_detail_ll.setVisibility(View.VISIBLE);

                }

            }
        });


        TextInputEditText occurance = (TextInputEditText) dialog.findViewById(R.id.new_bill_occurance);
        TextView submit = (TextView) dialog.findViewById(R.id.new_bill_add_submit);
        TextView close = (TextView) dialog.findViewById(R.id.new_bill_close);
        if (billingItemModel != null) {
            occurance.setVisibility(View.GONE);
            if ("PRODUCT".equalsIgnoreCase(billingItemModel.getType())) {
                productRadioButton.setChecked(true);
                nonProductRadioButton.setChecked(false);
                product_ll.setVisibility(View.VISIBLE);
                non_product_ll.setVisibility(View.GONE);

                if ("ATTAR".equalsIgnoreCase(billingItemModel.getProductCategory())) {
                    productCategoryAttarRadioButton.setChecked(true);
                    productCategorySprayRadioButton.setChecked(false);

                    if (getString(R.string.ML_6).equalsIgnoreCase(billingItemModel.getUnits() + "ML")) {
                        productQtySpinner.setSelection(0);
                    } else if (getString(R.string.ML_3).equalsIgnoreCase(billingItemModel.getUnits() + "ML")) {
                        productQtySpinner.setSelection(1);
                    } else if (getString(R.string.ML_12).equalsIgnoreCase(billingItemModel.getUnits() + "ML")) {
                        productQtySpinner.setSelection(2);
                    } else if (getString(R.string.ML_24).equalsIgnoreCase(billingItemModel.getUnits() + "ML")) {
                        productQtySpinner.setSelection(3);
                    }

                } else if ("SPRAY".equalsIgnoreCase(billingItemModel.getProductCategory())) {
                    productCategoryAttarRadioButton.setChecked(false);
                    productCategorySprayRadioButton.setChecked(true);

                    if (getString(R.string.ML_10).equalsIgnoreCase(billingItemModel.getUnits() + "ML")) {
                        productQtySpinner.setSelection(0);
                    } else if (getString(R.string.ML_30).equalsIgnoreCase(billingItemModel.getUnits() + "ML")) {
                        productQtySpinner.setSelection(1);
                    } else if (getString(R.string.ML_50).equalsIgnoreCase(billingItemModel.getUnits() + "ML")) {
                        productQtySpinner.setSelection(2);
                    } else if (getString(R.string.ML_100).equalsIgnoreCase(billingItemModel.getUnits() + "ML")) {
                        productQtySpinner.setSelection(3);
                    }
                }

                ProductModel selectProductModel = billingItemModel.getProductModel();
                if (selectProductModel != null) {
                    product_name.setText(selectProductModel.getName());
                    new_bill_owner.setText(selectProductModel.getOwner());
                    selectedProduct[0] = selectProductModel;
                    product_selling_cost.setText(String.valueOf(billingItemModel.getSellingItemPrice()));
                    product_detail_ll.setVisibility(View.VISIBLE);
                }
            } else if ("NON_PRODUCT".equalsIgnoreCase(billingItemModel.getType())) {
                nonProductRadioButton.setChecked(true);
                productRadioButton.setChecked(false);
                non_product_ll.setVisibility(View.VISIBLE);
                product_ll.setVisibility(View.GONE);
                non_product_name.setText(billingItemModel.getName());
                non_product_price.setText(String.valueOf(billingItemModel.getSellingItemPrice()));
                selectedNonProduct[0] = billingItemModel.getAccessoriesModel();

            }

        }
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (billingItemModel != null) {
                    billingItemModelList.remove(billingItemModel);
                    if ("PRODUCT".equalsIgnoreCase(type[0])) {
                        if (selectedProduct[0] == null) {
                            Toast.makeText(MainActivity.this, "Please Choose the Product Name..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (StringUtils.isEmpty(productQtyValue[0])) {
                            Toast.makeText(MainActivity.this, "Please Choose the Quantity..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (StringUtils.isEmpty(product_selling_cost.getText().toString())) {
                            Toast.makeText(MainActivity.this, "Please fill the Quantity..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        billingItemModel.setProductModel(selectedProduct[0]);
                        billingItemModel.setType(type[0]);
                        billingItemModel.setProductCategory(productCategoryType[0]);
                        billingItemModel.setName(selectedProduct[0].getName());
                        billingItemModel.setCode(selectedProduct[0].getCode());
                        Double fullPrice = Double.parseDouble(selectedProduct[0].getPrice());
                        billingItemModel.setUnitPrice(fullPrice / 1000);
                        if ("ATTAR".equalsIgnoreCase(billingItemModel.getProductCategory())) {
                            billingItemModel.setUnits(Integer.parseInt(productQtySpinner.getSelectedItem().toString().replace("ML", "")));
                            billingItemModel.setTotalPrice((Double.parseDouble(productQtySpinner.getSelectedItem().toString().replace("ML", "")) * (fullPrice / 1000)) + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                            billingItemModel.setSellingItemPrice(Double.valueOf(product_selling_cost.getText().toString()));
                        } else {
                            billingItemModel.setUnits(Integer.parseInt(productQtySpinner.getSelectedItem().toString().replace("ML", "")));
                            billingItemModel.setTotalPrice(getPerfumeActualPrice(Integer.parseInt(productQtySpinner.getSelectedItem().toString().replace("ML", "")), fullPrice / 1000) + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                            billingItemModel.setSellingItemPrice(Double.valueOf(product_selling_cost.getText().toString()));
                        }
                    } else if ("NON_PRODUCT".equalsIgnoreCase(type[0])) {
                        if (selectedNonProduct[0] == null) {
                            Toast.makeText(MainActivity.this, "Please Choose the Accessories Name..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (StringUtils.isEmpty(non_product_price.getText().toString())) {
                            Toast.makeText(MainActivity.this, "Please fill the Price..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        billingItemModel.setType(type[0]);
                        billingItemModel.setName(selectedNonProduct[0].getName());
                        billingItemModel.setTotalPrice(selectedNonProduct[0].getActualPrice() + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                        billingItemModel.setSellingItemPrice(Double.valueOf(non_product_price.getText().toString()));
                        billingItemModel.setAccessoriesModel(selectedNonProduct[0]);

                    }
                    billingItemModelList.add(billingItemModel);
                    billingAdapter.notifyDataSetChanged();
                    if (billingAdapter.getItemCount() > 0)
                        bill_recycler.post(() -> bill_recycler.scrollToPosition(billingAdapter.getItemCount() - 1));
                    manageBillingLayout();
                } else {
                    int iterCount = 0;
                    if (StringUtils.isBlank(occurance.getText().toString())) {
                        occurance.setText("1");
                    }
                    iterCount = Integer.valueOf(occurance.getText().toString());
                    for (int i = 0; i < iterCount; i++) {
                        BillingItemModel newBillingItemModel = new BillingItemModel();

                        if ("PRODUCT".equalsIgnoreCase(type[0])) {
                            if (selectedProduct[0] == null) {
                                Toast.makeText(MainActivity.this, "Please Choose the Product Name..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (StringUtils.isEmpty(productQtyValue[0])) {
                                Toast.makeText(MainActivity.this, "Please fill the Quantity..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            newBillingItemModel.setProductModel(selectedProduct[0]);
                            newBillingItemModel.setType(type[0]);
                            newBillingItemModel.setProductCategory(productCategoryType[0]);
                            newBillingItemModel.setName(selectedProduct[0].getName());
                            newBillingItemModel.setCode(selectedProduct[0].getCode());
                            Double fullPrice = Double.parseDouble(selectedProduct[0].getPrice());
                            newBillingItemModel.setUnitPrice(fullPrice / 1000);
                            if ("ATTAR".equalsIgnoreCase(newBillingItemModel.getProductCategory())) {
                                newBillingItemModel.setUnits(Integer.parseInt(productQtySpinner.getSelectedItem().toString().replace("ML", "")));
                                newBillingItemModel.setTotalPrice((Double.parseDouble(productQtySpinner.getSelectedItem().toString().replace("ML", "")) * (fullPrice / 1000)) + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                                newBillingItemModel.setSellingItemPrice(Double.valueOf(product_selling_cost.getText().toString()));
                            } else {
                                newBillingItemModel.setUnits(Integer.parseInt(productQtySpinner.getSelectedItem().toString().replace("ML", "")));
                                newBillingItemModel.setTotalPrice(getPerfumeActualPrice(Integer.parseInt(productQtySpinner.getSelectedItem().toString().replace("ML", "")), fullPrice / 1000) + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                                newBillingItemModel.setSellingItemPrice(Double.valueOf(product_selling_cost.getText().toString()));
                            }

                        } else if ("NON_PRODUCT".equalsIgnoreCase(type[0])) {

                            if (selectedNonProduct[0] == null) {
                                Toast.makeText(MainActivity.this, "Please Choose the Accessories Name..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (StringUtils.isEmpty(non_product_price.getText().toString())) {
                                Toast.makeText(MainActivity.this, "Please fill the Price..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            newBillingItemModel.setAccessoriesModel(selectedNonProduct[0]);
                            newBillingItemModel.setType(type[0]);
                            newBillingItemModel.setName(selectedNonProduct[0].getName());
                            newBillingItemModel.setTotalPrice(selectedNonProduct[0].getActualPrice() + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                            newBillingItemModel.setSellingItemPrice(Double.valueOf(non_product_price.getText().toString()));
                        }
                        billingItemModelList.add(newBillingItemModel);
                    }
                    billingAdapter.notifyDataSetChanged();
                    if (billingAdapter.getItemCount() > 0)
                        bill_recycler.post(() -> bill_recycler.scrollToPosition(billingAdapter.getItemCount() - 1));
                    manageBillingLayout();

                }
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private double getPerfumeActualPrice(int ml, double unitPrice) {
        double actualPerfumePrice = 0;
        ConfigModel configModel = sharedPrefHelper.getPerfumeActualMix();
        switch (ml) {
            case 10:
                actualPerfumePrice = unitPrice * configModel.getPerfume10mlMixer();
                break;
            case 30:
                actualPerfumePrice = unitPrice * configModel.getPerfume30mlMixer();
                break;
            case 50:
                actualPerfumePrice = unitPrice * configModel.getPerfume50mlMixer();
                break;
            case 100:
                actualPerfumePrice = unitPrice * configModel.getPerfume100mlMixer();
                break;
        }
        return actualPerfumePrice;
    }

    private void preAuthentication() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Admin Credential");

        // Set up the input
        final EditText input = new EditText(this);
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString();
                if (StringUtils.isEmpty(inputText)) {
                    Toast.makeText(MainActivity.this, "Wrong Password. try again later.!", Toast.LENGTH_LONG).show();
                    dialog.cancel();
                    return;
                }
                Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                dialog.cancel();
                if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void getServerDate() {
        Toast.makeText(MainActivity.this, "Loading.!", Toast.LENGTH_SHORT).show();

        OffsetDateTime offsetDateTime = null;
        if (StringUtils.isNotBlank(sharedPrefHelper.getSystemTime())) {
            offsetDateTime = OffsetDateTime.parse(sharedPrefHelper.getSystemTime()).withOffsetSameInstant(ZoneOffset.ofHoursMinutes(5, 30));
        } else {
            offsetDateTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.ofHoursMinutes(5, 30));
        }

        if (SingleTon.compareDateTime(offsetDateTime)) {
            Intent i = new Intent(MainActivity.this, InvoiceHistoryActivity.class);
            i.putExtra("owner", false);
            startActivity(i);
        } else {
            Toast.makeText(MainActivity.this, "Please check Mobile Date/Time", Toast.LENGTH_LONG).show();
        }

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
