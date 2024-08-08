package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.BillingViewAdapter;
import com.app.sha.attar.invoice.listener.BillingClickListener;
import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.DatabaseConstants;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.SharedConstants;
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    DrawerLayout mDrawerLayout;
    NavigationView navigationView;

    List<BillingItemModel> billingItemModelList = new ArrayList<>();

    List<ProductModel> productModelList = new ArrayList<>();
    List<AccessoriesModel> accessoriesModelList = new ArrayList<>();

    FrameLayout content_ll, empty_ll;

    RecyclerView bill_recycler;
    BillingViewAdapter billingAdapter;
    BillingClickListener listener;

    Context context;
    Activity activity;

    Button billing_add, billing_button;
    TextView billing_total_amount, billing_selling_amount, billing_discount;

    LinearLayout billing_discount_ll;

    EditText customer_name, customer_phone,remarks;
    TextView customer_search;

    Double totalAmount =  0.0, sellingAmount = 0.0, discount = 0.0;

    SharedPrefHelper sharedPrefHelper;
    DBUtil dbObj;
    FirebaseFirestore db;

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
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        TextView textView = (TextView) findViewById(R.id.home_nav_text_view);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preAuthentication();
            }
        });
        dbObj = new DBUtil();
        db = DBUtil.getInstance();
        sharedPrefHelper = new SharedPrefHelper(context);
        sharedPrefHelper.setTotalProductItem();
        sharedPrefHelper.setTotalAccessoriesItem();
        productModelList.addAll(sharedPrefHelper.getTotalProductList());
        accessoriesModelList.addAll(sharedPrefHelper.getTotalAccessoriesList());

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        content_ll = (FrameLayout) findViewById(R.id.home_content_ll);
        empty_ll = (FrameLayout) findViewById(R.id.home_empty_ll);

        billing_add = (Button) findViewById(R.id.home_bill_add_bt);
        billing_add.setOnClickListener(this);

        billing_total_amount = (TextView) findViewById(R.id.billing_total_amount_price);
        billing_selling_amount = (TextView) findViewById(R.id.billing_total_selling_price);
        billing_discount = (TextView) findViewById(R.id.billing_discount);
        billing_discount_ll = (LinearLayout) findViewById(R.id.billing_discount_ll);
        billing_button = (Button) findViewById(R.id.billing_submit_invoice);

        customer_name = (EditText) findViewById(R.id.billing_customer_name);
        customer_phone = (EditText) findViewById(R.id.billing_customer_phone);
        remarks = (EditText) findViewById(R.id.billing_customer_remarks);
        customer_search = (TextView) findViewById(R.id.billing_customer_search);

        customer_search.setOnClickListener(this);
        billing_discount_ll.setOnClickListener(this);
        billing_button.setOnClickListener(this);

        bill_recycler = (RecyclerView) findViewById(R.id.home_recyclerView);

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

        customer_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 10) {
                    Toast.makeText(context, "Searching .! ", Toast.LENGTH_LONG).show();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(customer_phone.getWindowToken(), 0);
                    searchContactInfo();

                }
            }
        });


        billingAdapter = new BillingViewAdapter(context, billingItemModelList, listener);
        bill_recycler.setLayoutManager(new LinearLayoutManager(this));
        bill_recycler.setAdapter(billingAdapter);

        manageBillingLayout();

    }

    private void searchContactInfo() {

        dbObj.getBillingInvoiceDetail(new FirestoreCallback<List<BillingInvoiceModel>>() {
            @Override
            public void onCallback(List<BillingInvoiceModel> aCustomerDetails) {
                System.out.println("customerHistorySize: " + aCustomerDetails.size());
                if(aCustomerDetails.size() >0){
                    customer_name.setText(aCustomerDetails.get(0).getCustomerName());
                }else{
                    Toast.makeText(context, "It's a new Customer .! ", Toast.LENGTH_LONG).show();
                }
            }
        }, customer_phone.getText().toString());
    }

    private boolean checkInternet() {
        if (SingleTon.isNetworkConnected(activity)) {
            return true;
        } else {
            Toast.makeText(context, "No Internet connection. Please try again .! ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    private void manageBillingLayout() {
        if (billingItemModelList.isEmpty()) {
            content_ll.setVisibility(View.GONE);
            empty_ll.setVisibility(View.VISIBLE);
            totalAmount = 0.0;
            discount = 0.0;
            customer_name.setText("");
            customer_phone.setText("");
            remarks.setText("");
            return;
        } else {
            content_ll.setVisibility(View.VISIBLE);
            empty_ll.setVisibility(View.GONE);
            totalAmount = 0.0;
            for (BillingItemModel billingItemModel : billingItemModelList) {
                totalAmount += billingItemModel.getSellingItemPrice();
            }
        }
        sellingAmount = totalAmount - ((totalAmount * discount) / 100);

        billing_total_amount.setText("Rs. " + totalAmount);
        billing_discount.setText("%  " + discount);
        billing_selling_amount.setText("Rs. " + sellingAmount);
        billingAdapter.notifyDataSetChanged();
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
        } else if (item.getItemId() == R.id.nav_report) {
            i = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_packaging) {
            i = new Intent(MainActivity.this, PackageActivity.class);
            startActivity(i);
        } else if (item.getItemId() == R.id.nav_customer) {
            i = new Intent(MainActivity.this, CustomerHistoryActivity.class);
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
        } else if (R.id.billing_discount_ll == view.getId()) {
            createDiscountDialog();
        } else if (R.id.billing_submit_invoice == view.getId()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (checkInternet())
                submitInvoiceDetails();
        } else if (R.id.billing_customer_search == view.getId()) {
            if (checkInternet())
                searchCustomerInfo();
        }

    }

    private void searchCustomerInfo() {
        //DB call with  customer_phone
        String phone_no = customer_phone.getText().toString();

        if (StringUtils.isEmpty(phone_no)) {
            Toast.makeText(MainActivity.this, "Enter Customer phone No ..!", Toast.LENGTH_LONG).show();
            return;
        }

        Intent customerIntent = new Intent(MainActivity.this, CustomerHistoryActivity.class);
        customerIntent.putExtra("phone_no", phone_no);
        startActivityForResult(customerIntent, 1);

    }


    private void submitInvoiceDetails() {

        if (StringUtils.isEmpty(customer_name.getText().toString())) {
            Toast.makeText(MainActivity.this, "Please Enter Customer Name..!", Toast.LENGTH_LONG).show();
            return;
        }
        if (StringUtils.isEmpty(customer_phone.getText().toString())) {
            Toast.makeText(MainActivity.this, "Please Enter Customer Phone no..!", Toast.LENGTH_LONG).show();
            return;
        }


        BillingInvoiceModel billingInvoiceModel = new BillingInvoiceModel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            billingInvoiceModel.setBillingDate(OffsetDateTime.now().toEpochSecond());
        }

        billingInvoiceModel.setCustomerName(customer_name.getText().toString());
        billingInvoiceModel.setCustomerPhone(customer_phone.getText().toString());
        billingInvoiceModel.setRemarks(remarks.getText().toString());
        billingInvoiceModel.setDiscount(discount);
        billingInvoiceModel.setSellingCost(sellingAmount);
        billingInvoiceModel.setTotalCost(totalAmount);
        billingInvoiceModel.setBillingItemModelList(billingItemModelList);
        billingItemModelList.stream().forEach(item -> {
            item.setInvoiceId(billingInvoiceModel.getBillingDate());
        });
        billingInvoiceModel.setBillingItemModelList(billingItemModelList);
        Toast.makeText(MainActivity.this, "Loading..!", Toast.LENGTH_LONG).show();
        db.collection(DatabaseConstants.INVOICE_COLLECTION)
                .document(String.valueOf(billingInvoiceModel.getBillingDate()))
                .set(billingInvoiceModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainActivity.this, "Submitted Successfully..!", Toast.LENGTH_LONG).show();
                        billingItemModelList.clear();
                        manageBillingLayout();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Internal server error..!", Toast.LENGTH_LONG).show();
                    }
                });
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
        TextView new_bill_code = (TextView) dialog.findViewById(R.id.new_bill_item_code);
        TextInputEditText product_size = (TextInputEditText) dialog.findViewById(R.id.new_bill_item_size);
        TextInputEditText product_selling_cost = (TextInputEditText) dialog.findViewById(R.id.new_bill_item_selling_price);


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
        TextInputEditText non_product_price = (TextInputEditText) dialog.findViewById(R.id.new_bill_non_product_selling_price);

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
                    new_bill_code.setText(selectProductModel.getCode());
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
                    new_bill_code.setText(selectProductModel.getCode());
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
                    billingItemModelList.remove(billingItemModel);
                    if ("PRODUCT".equalsIgnoreCase(type[0])) {
                        if (selectedProduct[0] == null) {
                            Toast.makeText(MainActivity.this, "Please Choose the Product Name..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (StringUtils.isEmpty(product_size.getText().toString())) {
                            Toast.makeText(MainActivity.this, "Please fill the Quantity..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (StringUtils.isEmpty(product_selling_cost.getText().toString())) {
                            Toast.makeText(MainActivity.this, "Please fill the Quantity..!", Toast.LENGTH_LONG).show();
                            return;
                        }
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
                            Toast.makeText(MainActivity.this, "Please Choose the Accessories Name..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (StringUtils.isEmpty(non_product_price.getText().toString())) {
                            Toast.makeText(MainActivity.this, "Please fill the Price..!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        billingItemModel.setType(type[0]);
                        billingItemModel.setName(selectedNonProduct[0].getName());
                        billingItemModel.setTotalPrice(selectedNonProduct[0].getPrice() + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                        billingItemModel.setSellingItemPrice(Double.valueOf(non_product_price.getText().toString()));
                        billingItemModel.setAccessoriesModel(selectedNonProduct[0]);

                    }
                    billingItemModelList.add(billingItemModel);
                    billingAdapter.notifyDataSetChanged();
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
                                Toast.makeText(MainActivity.this, "Please Choose the Product Name..!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (StringUtils.isEmpty(product_size.getText().toString())) {
                                Toast.makeText(MainActivity.this, "Please fill the Quantity..!", Toast.LENGTH_LONG).show();
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
                            newBillingItemModel.setTotalPrice(selectedNonProduct[0].getPrice() + Integer.valueOf(sharedPrefHelper.getPackageCost()));
                            newBillingItemModel.setSellingItemPrice(Double.valueOf(non_product_price.getText().toString()));
                        }
                        billingItemModelList.add(newBillingItemModel);
                    }
                    billingAdapter.notifyDataSetChanged();
                    manageBillingLayout();

                }
                dialog.dismiss();
            }
        });
        dialog.show();

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
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString();
                if (StringUtils.isEmpty(inputText) || !sharedPrefHelper.getPassword().equals(inputText)) {
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
}
