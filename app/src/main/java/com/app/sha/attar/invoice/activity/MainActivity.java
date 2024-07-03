package com.app.sha.attar.invoice.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.BillingViewAdapter;
import com.app.sha.attar.invoice.adapter.ProductViewAdapter;
import com.app.sha.attar.invoice.listener.BillingClickListener;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    DrawerLayout mDrawerLayout;
    NavigationView navigationView;

    List<BillingItemModel> billingItemModelList =new ArrayList<>();

    List<ProductModel> productModelList = new ArrayList<>();

    FrameLayout content_ll,empty_ll;

    RecyclerView bill_recycler;
    BillingViewAdapter billingAdapter;
    BillingClickListener listener;

    Context context;
    Activity activity;

    Button billing_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                if(!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });
        //
        //productModelList.add(new ProductModel(1,"Apple","A1",15000,"MTS","Y"));
        //productModelList.add(new ProductModel(2,"Banana","B1",25000,"MTS","Y"));
        //productModelList.add(new ProductModel(3,"Cherry","C1",5000,"IK","Y"));
        //productModelList.add(new ProductModel(4,"Data","D1",35000,"MTS","N"));
        //productModelList.add(new ProductModel(5,"Elderberry","E1",40000,"IK","Y"));

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        content_ll =(FrameLayout) findViewById(R.id.home_content_ll);
        empty_ll =(FrameLayout) findViewById(R.id.home_empty_ll);

        billing_add = (Button) findViewById(R.id.home_bill_add_bt);
        billing_add.setOnClickListener(this);

        bill_recycler =(RecyclerView) findViewById(R.id.home_recyclerView);

        listener =new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if("REMOVE".equalsIgnoreCase(type)){
                    billingItemModelList.remove(index);
                    billingAdapter.notifyDataSetChanged();
                    manageBillingLayout();
                }else if ("UPDATE".equalsIgnoreCase(type)){
                    createNewBillDialog(context,billingItemModelList.get(index));
                }
            }
        };

        billingAdapter= new BillingViewAdapter(context,billingItemModelList,listener);
        bill_recycler.setLayoutManager(new LinearLayoutManager(this));
        bill_recycler.setAdapter(billingAdapter);

        manageBillingLayout();

    }

    private void manageBillingLayout() {
        if (billingItemModelList.isEmpty()){
            content_ll.setVisibility(View.GONE);
            empty_ll.setVisibility(View.VISIBLE);
        }else{
            content_ll.setVisibility(View.VISIBLE);
            empty_ll.setVisibility(View.GONE);
        }
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
        Intent i =null;
        mDrawerLayout.closeDrawer(GravityCompat.START);
        if (item.getItemId() == R.id.nav_products){
            i = new Intent(MainActivity.this, ProductActivity.class);
            startActivity(i);
        }else if(item.getItemId() == R.id.nav_accessories){
            i = new Intent(MainActivity.this, AccessoriesActivity.class);
            startActivity(i);
        }else if(item.getItemId() == R.id.nav_report){
            i = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        }else if(item.getItemId() == R.id.nav_packaging)
        {
            i = new Intent(MainActivity.this, PackageActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if (R.id.home_bill_add_bt == view.getId()){
            createNewBillDialog(context,null);
        }

    }

    private void createNewBillDialog(Context context,BillingItemModel billingItemModel) {

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.dialog_billing_create);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        ProductModel[] selectedProduct = new ProductModel[1];

        LinearLayout product_ll = dialog.findViewById(R.id.new_bill_product_ll);
        LinearLayout non_product_ll = dialog.findViewById(R.id.new_bill_non_product_ll);
        LinearLayout product_detail_ll = dialog.findViewById(R.id.new_bill_detail_ll);

        TextView new_bill_owner = (TextView) dialog.findViewById(R.id.new_bill_item_owner);
        TextView new_bill_unit_price = (TextView) dialog.findViewById(R.id.new_bill_unit_price);
        TextView new_bill_code = (TextView) dialog.findViewById(R.id.new_bill_item_code);
        TextInputEditText product_size = (TextInputEditText) dialog.findViewById(R.id.new_bill_item_size);

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
                if(R.id.new_bill_product == checkedId){
                    product_ll.setVisibility(View.VISIBLE);
                    non_product_ll.setVisibility(View.GONE);
                    type[0] = "PRODUCT";
                }else if(R.id.new_bill_non_product == checkedId) {
                    product_ll.setVisibility(View.GONE);
                    non_product_ll.setVisibility(View.VISIBLE);
                    type[0] ="NON_PRODUCT";
                }
            }
        });

        TextInputEditText non_product_name = (TextInputEditText) dialog.findViewById(R.id.new_bill_non_product_name);
        TextInputEditText non_product_price = (TextInputEditText) dialog.findViewById(R.id.new_bill_non_product_price);

        AutoCompleteTextView product_name = (AutoCompleteTextView) dialog.findViewById(R.id.new_bill_name);

        List<String> items = productModelList.stream()
                .map(ProductModel::getName)
                .collect(Collectors.toList());


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

                if(resultModel.isPresent()){
                    ProductModel selectProductModel = resultModel.get();
                    product_name.setText(selectProductModel.getName());
                    new_bill_owner.setText(selectProductModel.getOwner());
                    new_bill_code.setText(selectProductModel.getCode());
                    Integer fullPrice = Integer.parseInt(selectProductModel.getPrice());
                    new_bill_unit_price.setText(String.valueOf(fullPrice/1000));
                    selectedProduct[0] = selectProductModel;
                    product_detail_ll.setVisibility(View.VISIBLE);

                }

            }
        });

        TextView submit = (TextView) dialog.findViewById(R.id.new_bill_add_submit);
        TextView close = (TextView) dialog.findViewById(R.id.new_bill_close);
        if (billingItemModel != null) {
            if("PRODUCT".equalsIgnoreCase(billingItemModel.getType())) {
                productRadioButton.setChecked(true);
                nonProductRadioButton.setChecked(false);
                product_ll.setVisibility(View.VISIBLE);
                non_product_ll.setVisibility(View.GONE);
                Optional<ProductModel> resultModel = productModelList.stream()
                        .filter(model -> billingItemModel.getName().equals(model.getName()))
                        .findFirst();

                if(resultModel.isPresent()){
                    ProductModel selectProductModel = resultModel.get();
                    product_name.setText(selectProductModel.getName());
                    new_bill_owner.setText(selectProductModel.getOwner());
                    new_bill_code.setText(selectProductModel.getCode());
                    Integer fullPrice = Integer.parseInt(selectProductModel.getPrice());
                    new_bill_unit_price.setText(String.valueOf(fullPrice/1000));
                    selectedProduct[0] = selectProductModel;
                    product_size.setText(String.valueOf(billingItemModel.getUnits()));
                }
            }else if("NON_PRODUCT".equalsIgnoreCase(billingItemModel.getType())){
                nonProductRadioButton.setChecked(true);
                productRadioButton.setChecked(false);
                non_product_ll.setVisibility(View.VISIBLE);
                product_ll.setVisibility(View.GONE);
                non_product_name.setText(billingItemModel.getName());
                non_product_price.setText(String.valueOf(billingItemModel.getTotalPrice()));
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
                    Toast.makeText(MainActivity.this, "Update Product ID - " + billingItemModel.getName(), Toast.LENGTH_LONG).show();
                    if("PRODUCT".equalsIgnoreCase(type[0])){
                        if(selectedProduct[0] == null){
                            Toast.makeText(MainActivity.this, "Please Choose the Product Name..!", Toast.LENGTH_LONG).show();
                        }
                        if(StringUtils.isEmpty(product_size.getText().toString())){
                            Toast.makeText(MainActivity.this, "Please fill the Quantity..!", Toast.LENGTH_LONG).show();

                        }
                        billingItemModel.setType(type[0]);
                        billingItemModel.setName(selectedProduct[0].getName());
                        billingItemModel.setCode(selectedProduct[0].getCode());
                        billingItemModel.setUnits(Integer.parseInt(product_size.getText().toString()));
                        Integer fullPrice = Integer.parseInt(selectedProduct[0].getPrice());
                        billingItemModel.setUnitPrice(fullPrice/1000);
                        billingItemModel.setTotalPrice((Integer.parseInt(product_size.getText().toString()) * (fullPrice/1000)) + 15);
                    }else if("NON_PRODUCT".equalsIgnoreCase(type[0])){
                        if(StringUtils.isEmpty(non_product_name.getText().toString())){
                            Toast.makeText(MainActivity.this, "Please Choose the accessories Name..!", Toast.LENGTH_LONG).show();
                        }
                        if(StringUtils.isEmpty(non_product_price.getText().toString())){
                            Toast.makeText(MainActivity.this, "Please fill the Price..!", Toast.LENGTH_LONG).show();

                        }
                        billingItemModel.setType(type[0]);
                        billingItemModel.setName(non_product_name.getText().toString());
                        billingItemModel.setTotalPrice(Integer.parseInt(non_product_price.getText().toString()));
                    }
                    billingItemModelList.add(billingItemModel);
                    billingAdapter.notifyDataSetChanged();
                    manageBillingLayout();
                } else {
                    BillingItemModel newBillingItemModel = new BillingItemModel();

                    if("PRODUCT".equalsIgnoreCase(type[0])){
                        if(selectedProduct[0] == null){
                            Toast.makeText(MainActivity.this, "Please Choose the Product Name..!", Toast.LENGTH_LONG).show();
                        }
                        if(StringUtils.isEmpty(product_size.getText().toString())){
                            Toast.makeText(MainActivity.this, "Please fill the Quantity..!", Toast.LENGTH_LONG).show();

                        }
                        newBillingItemModel.setType(type[0]);
                        newBillingItemModel.setName(selectedProduct[0].getName());
                        newBillingItemModel.setCode(selectedProduct[0].getCode());
                        newBillingItemModel.setUnits(Integer.parseInt(product_size.getText().toString()));
                        Integer fullPrice = Integer.parseInt(selectedProduct[0].getPrice());
                        newBillingItemModel.setUnitPrice(fullPrice/1000);
                        newBillingItemModel.setTotalPrice((Integer.parseInt(product_size.getText().toString()) * (fullPrice/1000)) + 15);
                    }else if("NON_PRODUCT".equalsIgnoreCase(type[0])){
                        if(StringUtils.isEmpty(non_product_name.getText().toString())){
                            Toast.makeText(MainActivity.this, "Please Choose the accessories Name..!", Toast.LENGTH_LONG).show();
                        }
                        if(StringUtils.isEmpty(non_product_price.getText().toString())){
                            Toast.makeText(MainActivity.this, "Please fill the Price..!", Toast.LENGTH_LONG).show();

                        }
                        newBillingItemModel.setType(type[0]);
                        newBillingItemModel.setName(non_product_name.getText().toString());
                        newBillingItemModel.setTotalPrice(Integer.parseInt(non_product_price.getText().toString()));
                    }
                    billingItemModelList.add(newBillingItemModel);
                    billingAdapter.notifyDataSetChanged();
                    manageBillingLayout();

                    Toast.makeText(MainActivity.this, "New product - " + newBillingItemModel.getName() + "-" + newBillingItemModel.getTotalPrice(), Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });
        dialog.show();

    }
}