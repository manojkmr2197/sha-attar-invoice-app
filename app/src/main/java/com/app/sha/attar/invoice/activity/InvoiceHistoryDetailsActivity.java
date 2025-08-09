package com.app.sha.attar.invoice.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.InvoiceHistoryDetailViewAdapter;
import com.app.sha.attar.invoice.listener.BillingClickListener;
import com.app.sha.attar.invoice.listener.TimeApi;
import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.model.TimeResponse;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.DatabaseConstants;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.RetrofitClient;
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InvoiceHistoryDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;
    DBUtil dbObj;
    FirebaseFirestore db;
    BillingInvoiceModel billingInvoiceModel;

    TextView back, invoiceIdTv, invoiceDtTv;
    TextView customerName, customerPhone;

    RadioGroup paymentGroup;
    RadioButton cashRadioBt,upiRadioBt;
    String paymentMode="";

    RecyclerView itemRecyclerview;

    InvoiceHistoryDetailViewAdapter invoiceAdapter;
    BillingClickListener clickListener;


    TextView totalAmountTv, discountTv, sellingAmountTv;
    Button addItemBt, addInvoiceBt;

    List<BillingItemModel> itemModelList = new ArrayList<>();


    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);
    DecimalFormat df = new DecimalFormat("#.00");

    OffsetDateTime offsetDateTime;

    Double totalAmount =  0.0, sellingAmount = 0.0, discount = 0.0;


    List<ProductModel> productModelList = new ArrayList<>();
    List<AccessoriesModel> accessoriesModelList = new ArrayList<>();
    SharedPrefHelper sharedPrefHelper;

    Boolean owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_invoice_history_details);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }
        context = InvoiceHistoryDetailsActivity.this;
        activity = InvoiceHistoryDetailsActivity.this;
        dbObj = new DBUtil();
        db = DBUtil.getInstance();
        sharedPrefHelper = new SharedPrefHelper(context);
        productModelList.addAll(sharedPrefHelper.getTotalProductList());
        accessoriesModelList.addAll(sharedPrefHelper.getTotalAccessoriesList());

        back = (TextView) findViewById(R.id.invoice_history_detail_back);
        back.setOnClickListener(this);

        invoiceIdTv = (TextView) findViewById(R.id.invoice_history_detail_id);
        invoiceDtTv = (TextView) findViewById(R.id.invoice_history_detail_date_tv);
        invoiceDtTv.setOnClickListener(this);
        customerName = (TextView) findViewById(R.id.invoice_history_detail_customer_name);
        customerPhone = (TextView) findViewById(R.id.invoice_history_detail_customer_phone);

        paymentGroup = (RadioGroup) findViewById(R.id.invoice_history_detail_payment_radio_group);
        cashRadioBt = (RadioButton) findViewById(R.id.invoice_history_detail_payment_cash);
        upiRadioBt = (RadioButton) findViewById(R.id.invoice_history_detail_payment_upi);

        cashRadioBt.setChecked(true);
        paymentMode = "CASH";
        paymentGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Find which radio button is selected
                if (R.id.invoice_history_detail_payment_cash == checkedId) {
                    paymentMode = "CASH";
                } else if (R.id.invoice_history_detail_payment_upi == checkedId) {
                    paymentMode = "UPI";
                }
            }
        });


        itemRecyclerview = (RecyclerView) findViewById(R.id.invoice_history_detail_recycler_view);
        clickListener =new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if("EDIT".equalsIgnoreCase(type)){
                    createNewBillDialog(context,itemModelList.get(index));
                }else if("DELETE".equalsIgnoreCase(type)){
                    askItemDeleteConfirmation(index);
                }
            }
        };
        invoiceAdapter =new InvoiceHistoryDetailViewAdapter(context,itemModelList,clickListener);
        itemRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        itemRecyclerview.setAdapter(invoiceAdapter);

        invoiceAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                itemRecyclerview.scrollToPosition((itemModelList.size()>0)?itemModelList.size() - 1:0);
            }
        });

        totalAmountTv = (TextView) findViewById(R.id.invoice_history_detail_total_amount_price);
        sellingAmountTv = (TextView) findViewById(R.id.invoice_history_detail_total_selling_price);
        discountTv = (TextView) findViewById(R.id.invoice_history_detail_discount);
        discountTv.setOnClickListener(this);

        addItemBt = (Button) findViewById(R.id.invoice_history_detail_add_item_bt);
        addItemBt.setOnClickListener(this);
        addInvoiceBt = (Button) findViewById(R.id.invoice_history_detail_submit);
        addInvoiceBt.setOnClickListener(this);

        Intent intent = getIntent();
        String invoiceId = intent.getStringExtra("invoiceId");
        owner = intent.getBooleanExtra("owner",false);

        if (StringUtils.isNoneBlank(invoiceId)) {
            addInvoiceBt.setText("Update Invoice");
            getInvoiceDocumentDetails(invoiceId);
        }else{
            addInvoiceBt.setText("Add Invoice");
            billingInvoiceModel = new BillingInvoiceModel();
            customerName.setText(sharedPrefHelper.getLoginUserName());
            customerPhone.setText(sharedPrefHelper.getLoginUserPhone());
            getServerDate();
        }

    }

    private void getServerDate() {

        if(StringUtils.isNotBlank(sharedPrefHelper.getSystemTime())) {
            offsetDateTime = OffsetDateTime.parse(sharedPrefHelper.getSystemTime()).withOffsetSameInstant(ZoneOffset.ofHoursMinutes(5, 30));
        }else {
            offsetDateTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.ofHoursMinutes(5, 30));
        }

    }


    private void askItemDeleteConfirmation(int index) {

        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to remove this item?");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            itemModelList.remove(index);
            manageBillingLayout();
        });

        // Set negative button
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void getInvoiceDocumentDetails(String invoiceId) {
        dbObj.getBillingItemDetailByDocId(new FirestoreCallback<BillingInvoiceModel>() {
            @Override
            public void onCallback(BillingInvoiceModel result) {
                billingInvoiceModel = result;
                invoiceIdTv.setText("Invoice ID : " + billingInvoiceModel.getBillingDate());
                customerName.setText(billingInvoiceModel.getCustomerName());
                customerPhone.setText(billingInvoiceModel.getCustomerPhone());
                paymentMode =billingInvoiceModel.getPaymentMode();
                if("CASH".equalsIgnoreCase(paymentMode)){
                    cashRadioBt.setChecked(true);
                    upiRadioBt.setChecked(false);
                }else{
                    cashRadioBt.setChecked(false);
                    upiRadioBt.setChecked(true);
                }

                totalAmountTv.setText("Rs. " + df.format(billingInvoiceModel.getTotalCost()));
                discountTv.setText(billingInvoiceModel.getDiscount() + "%");
                sellingAmountTv.setText("Rs. " + df.format(billingInvoiceModel.getSellingCost()));

                totalAmount =billingInvoiceModel.getTotalCost();
                discount =billingInvoiceModel.getDiscount();
                sellingAmount = billingInvoiceModel.getSellingCost();

                itemModelList.clear();
                itemModelList.addAll(result.getBillingItemModelList());
                invoiceAdapter.notifyDataSetChanged();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    OffsetDateTime offsetDateTime = Instant.ofEpochSecond(result.getBillingDate()).atOffset(istOffset);
                    invoiceDtTv.setText(offsetDateTime.format(formatter));
                    invoiceDtTv.setClickable(false);
                    invoiceDtTv.setEnabled(false);
                }
            }
        }, invoiceId);
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
    public void onClick(View view) {
        if (R.id.invoice_history_detail_back == view.getId()) {
            //close confirmation popup
            closeConfirmationPopup();
        } else if (R.id.invoice_history_detail_submit == view.getId()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (checkInternet())
                submitInvoiceDetails();
        } else if (R.id.invoice_history_detail_add_item_bt == view.getId()) {
            createNewBillDialog(context,null);
        } else if (R.id.invoice_history_detail_discount == view.getId()) {
            createDiscountDialog();
        } else if (R.id.invoice_history_detail_date_tv == view.getId()) {
            chooseDateTimePicker();
        }
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

    private void manageBillingLayout() {
        totalAmount = 0.0;
        for (BillingItemModel billingItemModel : itemModelList) {
            totalAmount += billingItemModel.getSellingItemPrice();
        }
        sellingAmount = totalAmount - ((totalAmount * discount) / 100);
        totalAmountTv.setText("Rs. " + totalAmount);
        discountTv.setText(discount+" %");
        sellingAmountTv.setText("Rs. " + sellingAmount);
        invoiceAdapter.notifyDataSetChanged();
    }

    private void createNewBillDialog(Context context, BillingItemModel billingItemModel) {

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.dialog_billing_create);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

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
        productQtySpinner.setAdapter(attarAdapter);
        productQtySpinner.setSelection(0);
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
                if(selectedProduct[0] != null){
                    if(productCategoryType[0].equalsIgnoreCase("ATTAR")){
                        product_selling_cost.setText(""+selectedProduct[0].getAttarSellingPriceMap().get(selectedValue));
                        productQtyValue[0] = selectedValue;
                    }else if(productCategoryType[0].equalsIgnoreCase("SPRAY")){
                        product_selling_cost.setText(""+selectedProduct[0].getPerfumeSellingPriceMap().get(selectedValue));
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

                if("ATTAR".equalsIgnoreCase(billingItemModel.getProductCategory())){
                    productCategoryAttarRadioButton.setChecked(true);
                    productCategorySprayRadioButton.setChecked(false);

                    if (getString(R.string.ML_6).equalsIgnoreCase(billingItemModel.getUnits()+"ML")) {
                        productQtySpinner.setSelection(0);
                    } else if (getString(R.string.ML_3).equalsIgnoreCase(billingItemModel.getUnits()+"ML"))  {
                        productQtySpinner.setSelection(1);
                    }else if (getString(R.string.ML_12).equalsIgnoreCase(billingItemModel.getUnits()+"ML"))  {
                        productQtySpinner.setSelection(2);
                    }else if (getString(R.string.ML_24).equalsIgnoreCase(billingItemModel.getUnits()+"ML"))  {
                        productQtySpinner.setSelection(3);
                    }

                }else if("SPRAY".equalsIgnoreCase(billingItemModel.getProductCategory())){
                    productCategoryAttarRadioButton.setChecked(false);
                    productCategorySprayRadioButton.setChecked(true);

                    if (getString(R.string.ML_10).equalsIgnoreCase(billingItemModel.getUnits()+"ML")) {
                        productQtySpinner.setSelection(0);
                    } else if (getString(R.string.ML_30).equalsIgnoreCase(billingItemModel.getUnits()+"ML"))  {
                        productQtySpinner.setSelection(1);
                    }else if (getString(R.string.ML_50).equalsIgnoreCase(billingItemModel.getUnits()+"ML"))  {
                        productQtySpinner.setSelection(2);
                    }else if (getString(R.string.ML_100).equalsIgnoreCase(billingItemModel.getUnits()+"ML"))  {
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
                    itemModelList.remove(billingItemModel);
                    if ("PRODUCT".equalsIgnoreCase(type[0])) {
                        if (selectedProduct[0] == null) {
                            Toast.makeText(InvoiceHistoryDetailsActivity.this, "Please Choose the Product Name..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (StringUtils.isEmpty(productQtyValue[0])){
                            Toast.makeText(InvoiceHistoryDetailsActivity.this, "Please Choose the Quantity..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (StringUtils.isEmpty(product_selling_cost.getText().toString())) {
                            Toast.makeText(InvoiceHistoryDetailsActivity.this, "Please fill the Quantity..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        billingItemModel.setProductModel(selectedProduct[0]);
                        billingItemModel.setType(type[0]);
                        billingItemModel.setProductCategory(productCategoryType[0]);
                        billingItemModel.setName(selectedProduct[0].getName());
                        billingItemModel.setCode(selectedProduct[0].getCode());
                        Double fullPrice = Double.parseDouble(selectedProduct[0].getPrice());
                        billingItemModel.setUnitPrice(fullPrice / 1000);
                        if("ATTAR".equalsIgnoreCase(billingItemModel.getProductCategory())){
                            billingItemModel.setUnits(Integer.parseInt(productQtySpinner.getSelectedItem().toString().replace("ML", "")));
                            billingItemModel.setTotalPrice((Double.parseDouble(productQtySpinner.getSelectedItem().toString().replace("ML", "")) * (fullPrice / 1000)) + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                            billingItemModel.setSellingItemPrice(Double.valueOf(product_selling_cost.getText().toString()));
                        }else {
                            billingItemModel.setUnits(Integer.parseInt(productQtySpinner.getSelectedItem().toString().replace("ML", "")));
                            billingItemModel.setTotalPrice((selectedProduct[0].getPerfumeActualPriceMap().get(productQtySpinner.getSelectedItem().toString())) + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                            billingItemModel.setSellingItemPrice(Double.valueOf(product_selling_cost.getText().toString()));
                        }
                    } else if ("NON_PRODUCT".equalsIgnoreCase(type[0])) {
                        if (selectedNonProduct[0] == null) {
                            Toast.makeText(InvoiceHistoryDetailsActivity.this, "Please Choose the Accessories Name..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (StringUtils.isEmpty(non_product_price.getText().toString())) {
                            Toast.makeText(InvoiceHistoryDetailsActivity.this, "Please fill the Price..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        billingItemModel.setType(type[0]);
                        billingItemModel.setName(selectedNonProduct[0].getName());
                        billingItemModel.setTotalPrice(selectedNonProduct[0].getActualPrice());
                        billingItemModel.setSellingItemPrice(Double.valueOf(non_product_price.getText().toString()));
                        billingItemModel.setAccessoriesModel(selectedNonProduct[0]);

                    }
                    itemModelList.add(billingItemModel);
                    invoiceAdapter.notifyDataSetChanged();
                    manageBillingLayout();
                } else {
                    int iterCount =0;
                    if(StringUtils.isBlank(occurance.getText().toString())){
                        occurance.setText("1");
                    }
                    iterCount = Integer.valueOf(occurance.getText().toString());
                    for (int i=0;i<iterCount;i++) {
                        BillingItemModel newBillingItemModel = new BillingItemModel();

                        if ("PRODUCT".equalsIgnoreCase(type[0])) {
                            if (selectedProduct[0] == null) {
                                Toast.makeText(InvoiceHistoryDetailsActivity.this, "Please Choose the Product Name..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (StringUtils.isEmpty(productQtyValue[0])) {
                                Toast.makeText(InvoiceHistoryDetailsActivity.this, "Please fill the Quantity..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            newBillingItemModel.setProductModel(selectedProduct[0]);
                            newBillingItemModel.setType(type[0]);
                            newBillingItemModel.setProductCategory(productCategoryType[0]);
                            newBillingItemModel.setName(selectedProduct[0].getName());
                            newBillingItemModel.setCode(selectedProduct[0].getCode());
                            Double fullPrice = Double.parseDouble(selectedProduct[0].getPrice());
                            newBillingItemModel.setUnitPrice(fullPrice / 1000);
                            if("ATTAR".equalsIgnoreCase(newBillingItemModel.getProductCategory())){
                                newBillingItemModel.setUnits(Integer.parseInt(productQtySpinner.getSelectedItem().toString().replace("ML","")));
                                newBillingItemModel.setTotalPrice((Double.parseDouble(productQtySpinner.getSelectedItem().toString().replace("ML", "")) * (fullPrice / 1000)) + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                                newBillingItemModel.setSellingItemPrice(Double.valueOf(product_selling_cost.getText().toString()));
                            }else{
                                newBillingItemModel.setUnits(Integer.parseInt(productQtySpinner.getSelectedItem().toString().replace("ML","")));
                                newBillingItemModel.setTotalPrice((Double.valueOf(selectedProduct[0].getPerfumeActualPriceMap().get(productQtySpinner.getSelectedItem().toString()))) + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                                newBillingItemModel.setSellingItemPrice(Double.valueOf(product_selling_cost.getText().toString()));
                            }

                        } else if ("NON_PRODUCT".equalsIgnoreCase(type[0])) {

                            if (selectedNonProduct[0] == null) {
                                Toast.makeText(InvoiceHistoryDetailsActivity.this, "Please Choose the Accessories Name..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (StringUtils.isEmpty(non_product_price.getText().toString())) {
                                Toast.makeText(InvoiceHistoryDetailsActivity.this, "Please fill the Price..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            newBillingItemModel.setAccessoriesModel(selectedNonProduct[0]);
                            newBillingItemModel.setType(type[0]);
                            newBillingItemModel.setName(selectedNonProduct[0].getName());
                            newBillingItemModel.setTotalPrice(selectedNonProduct[0].getActualPrice());
                            newBillingItemModel.setSellingItemPrice(Double.valueOf(non_product_price.getText().toString()));
                        }
                        itemModelList.add(newBillingItemModel);
                    }
                    invoiceAdapter.notifyDataSetChanged();
                    manageBillingLayout();

                }
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void createNewBillDialogOLD(Context context, BillingItemModel billingItemModel) {

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.dialog_billing_create);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        ProductModel[] selectedProduct = new ProductModel[1];
        AccessoriesModel[] selectedNonProduct = new AccessoriesModel[1];

        LinearLayout product_ll = dialog.findViewById(R.id.new_bill_product_ll);
        LinearLayout non_product_ll = dialog.findViewById(R.id.new_bill_non_product_ll);
        LinearLayout product_detail_ll = dialog.findViewById(R.id.new_bill_detail_ll);

        TextView new_bill_owner = (TextView) dialog.findViewById(R.id.new_bill_item_owner);
        TextInputEditText product_size = (TextInputEditText) dialog.findViewById(R.id.new_bill_item_size);
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


        AutoCompleteTextView non_product_name = (AutoCompleteTextView) dialog.findViewById(R.id.new_bill_non_product_name);
        TextView non_product_price = (TextView) dialog.findViewById(R.id.new_bill_non_product_selling_price);

        List<String> accessories_items = accessoriesModelList.stream()
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

                ProductModel selectProductModel = billingItemModel.getProductModel();
                if (selectProductModel != null) {
                    product_name.setText(selectProductModel.getName());
                    new_bill_owner.setText(selectProductModel.getOwner());
                    selectedProduct[0] = selectProductModel;
                    product_size.setText(String.valueOf(billingItemModel.getUnits()));
                    product_selling_cost.setText(String.valueOf(billingItemModel.getSellingItemPrice()));
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
                    //itemModelList.remove(billingItemModel);
                    if ("PRODUCT".equalsIgnoreCase(type[0])) {
                        if (selectedProduct[0] == null) {
                            Toast.makeText(context, "Please Choose the Product Name..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (StringUtils.isEmpty(product_size.getText().toString())) {
                            Toast.makeText(context, "Please fill the Quantity..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (StringUtils.isEmpty(product_selling_cost.getText().toString())) {
                            Toast.makeText(context, "Please fill the Quantity..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        billingItemModel.setAccessoriesModel(null);
                        billingItemModel.setProductModel(selectedProduct[0]);
                        billingItemModel.setType(type[0]);
                        billingItemModel.setName(selectedProduct[0].getName());
                        billingItemModel.setCode(selectedProduct[0].getCode());
                        billingItemModel.setUnits(Integer.parseInt(product_size.getText().toString()));
                        Double fullPrice = Double.parseDouble(selectedProduct[0].getPrice());
                        billingItemModel.setUnitPrice(fullPrice / 1000);
                        billingItemModel.setTotalPrice((Double.parseDouble(product_size.getText().toString()) * (fullPrice / 1000)) + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                        billingItemModel.setSellingItemPrice(Double.valueOf(product_selling_cost.getText().toString()));
                    } else if ("NON_PRODUCT".equalsIgnoreCase(type[0])) {
                        if (selectedNonProduct[0] == null) {
                            Toast.makeText(context, "Please Choose the Accessories Name..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (StringUtils.isEmpty(non_product_price.getText().toString())) {
                            Toast.makeText(context, "Please fill the Price..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        billingItemModel.setType(type[0]);
                        billingItemModel.setName(selectedNonProduct[0].getName());
                        billingItemModel.setTotalPrice(selectedNonProduct[0].getActualPrice() + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                        billingItemModel.setSellingItemPrice(Double.valueOf(non_product_price.getText().toString()));
                        billingItemModel.setAccessoriesModel(selectedNonProduct[0]);
                        billingItemModel.setProductModel(null);
                    }
                    //itemModelList.add(billingItemModel);
                    invoiceAdapter.notifyDataSetChanged();
                    manageBillingLayout();
                } else {
                    int iterCount =0;
                    if(StringUtils.isBlank(occurance.getText().toString())){
                        occurance.setText("1");
                    }
                    iterCount = Integer.valueOf(occurance.getText().toString());
                    for (int i=0;i<iterCount;i++) {
                        BillingItemModel newBillingItemModel = new BillingItemModel();

                        if ("PRODUCT".equalsIgnoreCase(type[0])) {
                            if (selectedProduct[0] == null) {
                                Toast.makeText(context, "Please Choose the Product Name..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (StringUtils.isEmpty(product_size.getText().toString())) {
                                Toast.makeText(context, "Please fill the Quantity..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            newBillingItemModel.setProductModel(selectedProduct[0]);
                            newBillingItemModel.setType(type[0]);
                            newBillingItemModel.setName(selectedProduct[0].getName());
                            newBillingItemModel.setCode(selectedProduct[0].getCode());
                            newBillingItemModel.setUnits(Integer.parseInt(product_size.getText().toString()));
                            Double fullPrice = Double.parseDouble(selectedProduct[0].getPrice());
                            newBillingItemModel.setUnitPrice(fullPrice / 1000);
                            newBillingItemModel.setTotalPrice((Double.parseDouble(product_size.getText().toString()) * (fullPrice / 1000)) + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                            newBillingItemModel.setSellingItemPrice(Double.valueOf(product_selling_cost.getText().toString()));
                        } else if ("NON_PRODUCT".equalsIgnoreCase(type[0])) {

                            if (selectedNonProduct[0] == null) {
                                Toast.makeText(context, "Please Choose the Accessories Name..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (StringUtils.isEmpty(non_product_price.getText().toString())) {
                                Toast.makeText(context, "Please fill the Price..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            newBillingItemModel.setAccessoriesModel(selectedNonProduct[0]);
                            newBillingItemModel.setType(type[0]);
                            newBillingItemModel.setName(selectedNonProduct[0].getName());
                            newBillingItemModel.setTotalPrice(selectedNonProduct[0].getActualPrice() + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                            newBillingItemModel.setSellingItemPrice(Double.valueOf(non_product_price.getText().toString()));
                        }
                        itemModelList.add(newBillingItemModel);
                    }
                    invoiceAdapter.notifyDataSetChanged();
                    manageBillingLayout();

                }
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void chooseDateTimePicker() {
        // Get current date and time
        Calendar calendar = Calendar.getInstance();

        // DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Update calendar with selected date
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // TimePickerDialog
                    new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                // Update calendar with selected time
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                // Display selected date and time
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());

                                LocalDateTime localDatetime = calendar.toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();

                                // Create OffsetDateTime with 00:00 time
                                offsetDateTime = localDatetime.atZone(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
                                billingInvoiceModel.setBillingDate(offsetDateTime.toEpochSecond());
                                invoiceDtTv.setText(offsetDateTime.format(formatter));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false // 24-hour format
                    ).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        if(!owner && "Select Date".equalsIgnoreCase(invoiceDtTv.getText().toString())){
            datePickerDialog.getDatePicker().setMinDate(offsetDateTime.minusDays(1).toInstant().toEpochMilli());
        }
        datePickerDialog.getDatePicker().setMaxDate(offsetDateTime.toInstant().toEpochMilli());
        datePickerDialog.show();

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        closeConfirmationPopup();
    }

    private void closeConfirmationPopup() {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to exit without saving?");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });

        // Set negative button
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void submitInvoiceDetails() {

        if(billingInvoiceModel.getBillingDate() == null){
            Toast.makeText(context, "Please Select Invoice Date..!", Toast.LENGTH_LONG).show();
            return;
        }

        if(itemModelList.isEmpty()){
            Toast.makeText(context, "Please Add products / Accessories..!", Toast.LENGTH_LONG).show();
            return;
        }
        if(StringUtils.isBlank(paymentMode)){
            Toast.makeText(context, "Please Choose Payment mode..!", Toast.LENGTH_LONG).show();
            return;
        }


        billingInvoiceModel.setCustomerName(customerName.getText().toString());
        billingInvoiceModel.setCustomerPhone(customerPhone.getText().toString());
        billingInvoiceModel.setPaymentMode(paymentMode);
        billingInvoiceModel.setDiscount(discount);
        billingInvoiceModel.setSellingCost(sellingAmount);
        billingInvoiceModel.setTotalCost(totalAmount);
        billingInvoiceModel.setBillingItemModelList(itemModelList);
        itemModelList.stream().forEach(item -> {
            item.setInvoiceId(billingInvoiceModel.getBillingDate());
        });
        billingInvoiceModel.setBillingItemModelList(itemModelList);
        Toast.makeText(context, "Loading..!", Toast.LENGTH_LONG).show();
        db.collection(DatabaseConstants.INVOICE_COLLECTION)
                .document(String.valueOf(billingInvoiceModel.getBillingDate()))
                .set(billingInvoiceModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Submitted Successfully..!", Toast.LENGTH_LONG).show();
                        itemModelList.clear();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Internal server error..!", Toast.LENGTH_LONG).show();
                    }
                });
    }

}