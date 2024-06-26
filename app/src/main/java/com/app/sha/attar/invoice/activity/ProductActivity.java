package com.app.sha.attar.invoice.activity;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.ProductViewAdapter;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import com.app.sha.attar.invoice.utils.DBUtil;

public class ProductActivity extends AppCompatActivity implements View.OnClickListener {

    FrameLayout data_fl,no_data_fl;
    Context context;
    Activity activity;

    List<ProductModel> itemList =new ArrayList<>();
    List<ProductModel> filteredList =new ArrayList<>();

    ProductViewAdapter productAdapter;
    RecyclerView recyclerView;
    ClickListener listener;

    TextInputEditText search_et;
    Spinner ownerSpinner;
    Button search_bt;

    String searchText,searchOwner;
    DBUtil dbObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        context = ProductActivity.this;
        activity = ProductActivity.this;

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }

        no_data_fl = (FrameLayout) findViewById(R.id.product_no_data_ll);
        data_fl = (FrameLayout) findViewById(R.id.product_data_ll);

        search_et = (TextInputEditText) findViewById(R.id.product_search_et);
        ownerSpinner = (Spinner) findViewById(R.id.product_spinner);
        search_bt = (Button) findViewById(R.id.product_search);
        search_bt.setOnClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.product_recyclerView);
        TextView back = (TextView) findViewById(R.id.product_back);
        back.setOnClickListener(this);

        FloatingActionButton add_fab = (FloatingActionButton) findViewById(R.id.product_add_fab);
        add_fab.setOnClickListener(this);
        dbObj = new DBUtil();
        listener = new ClickListener() {
            @Override
            public void click(int index) {
                createDialogBox(context, itemList.get(index));
            }
        };
        checkInternet();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_items, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ownerSpinner.setAdapter(adapter);

    }

    private void checkInternet() {
        if(SingleTon.isNetworkConnected(activity)){
            no_data_fl.setVisibility(View.GONE);
            data_fl.setVisibility(View.VISIBLE);
            callApiData();
        }else{
            no_data_fl.setVisibility(View.VISIBLE);
            data_fl.setVisibility(View.GONE);
            Toast.makeText(context, "No Internet connection. Please try again .! ", Toast.LENGTH_LONG).show();
        }

    }

    private void callApiData() {
        //itemList = new ArrayList<>();
        dbObj.GetAllProducts(new FirestoreCallback() {
            @Override
            public void onCallback(List<ProductModel> i_itemList) {
                System.out.println("Sabeek.size:"+i_itemList.size());
                itemList.clear();
                itemList.addAll(i_itemList);
            }
        });
        //itemList = new ArrayList<>();
       /* itemList = dbObj.GetAllProducts();
        if(itemList.isEmpty())
        {
            itemList.add(new ProductModel(1,"Apple","A1","15","MTS","Y"));
            itemList.add(new ProductModel(2,"Banana","B1","25","MTS","Y"));
            itemList.add(new ProductModel(3,"Cherry","C1","5","IK","Y"));
            itemList.add(new ProductModel(4,"Data","D1","35","MTS","N"));
            itemList.add(new ProductModel(5,"Elderberry","E1","40","IK","Y"));

        }
        */
        filteredList.addAll(itemList);
        productAdapter= new ProductViewAdapter(context,filteredList,listener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(productAdapter);
    }

    @Override
    public void onClick(View view) {
        if(R.id.product_back == view.getId()){
            finish();
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
        }else if(R.id.product_add_fab == view.getId()){
            createDialogBox(ProductActivity.this, null);
        }else if(R.id.product_search == view.getId()){
            Log.v("data1 -- >", search_et.getText().toString());
            Log.v("data2 -- >", ownerSpinner.getSelectedItem().toString());

            searchText =search_et.getText().toString();
            searchOwner =ownerSpinner.getSelectedItem().toString();
            if(searchOwner.equalsIgnoreCase("ALL")){
                searchOwner = "";
            }
            filter(searchText, searchOwner);

        }
    }

    private void createDialogBox(Context context, ProductModel productModel) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.dialog_product_create);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        TextInputEditText name = (TextInputEditText) dialog.findViewById(R.id.product_add_name);
        TextInputEditText price = (TextInputEditText) dialog.findViewById(R.id.product_add_price);
        Spinner owner = (Spinner) dialog.findViewById(R.id.product_add_owner);
        CheckBox available = (CheckBox) dialog.findViewById(R.id.product_add_checkbox);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_owners, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        owner.setAdapter(adapter);

        Button submit = (Button) dialog.findViewById(R.id.product_add_submit);
        TextView close = (TextView) dialog.findViewById(R.id.product_add_close);
        if (productModel != null) {
            name.setText(productModel.getName());
            price.setText(productModel.getPrice());
            if("MTS".equalsIgnoreCase(productModel.getOwner())) {
                owner.setSelection(0);
            }else if("IK".equalsIgnoreCase(productModel.getOwner())){
                owner.setSelection(1);
            }
            if("Y".equalsIgnoreCase(productModel.getIsavailable())){
                available.setChecked(true);
            }else{
                available.setChecked(false);
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
                if (productModel != null) {
                    Toast.makeText(ProductActivity.this, "Update Product ID - " + productModel.getId(), Toast.LENGTH_LONG).show();
                } else {

                    //Toast.makeText(ProductActivity.this, "New Product ID - " + productModel.getName(), Toast.LENGTH_LONG).show();

                    //sabeek
                    String strName = name.getText().toString();
                    String strPrice = price.getText().toString();
                    String strOwner = owner.getSelectedItem().toString();
                    String strAvailStatus = available.isChecked() ? "Y" : "N";
                    Boolean isSavedSuccessfully = FALSE;

                    isSavedSuccessfully = dbObj.AddProduct(strName,strPrice,strOwner,strAvailStatus);
                    if(isSavedSuccessfully == TRUE)
                    {
                        Toast.makeText(ProductActivity.this, "New product - " + name.getText() + "-" + price.getText(), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(ProductActivity.this, "Error while adding new product product - " + name.getText() + "-" + price.getText(), Toast.LENGTH_LONG).show();
                    }

                }

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void filter(String text, String owner) {
        filteredList.clear();
        text = (text == null) ? "" : text;
        owner = (owner == null) ? "" : owner;
        if (text.isEmpty() && owner.isEmpty()) {
            filteredList.addAll(itemList);
        } else if (!text.isEmpty() && owner.isEmpty()) {
            text = text.toLowerCase();
            for (ProductModel item : itemList) {
                if (item.getName().toLowerCase().contains(text)) {
                    filteredList.add(item);
                }
            }
        } else if (text.isEmpty() && !owner.isEmpty()) {
            owner = owner.toLowerCase();
            for (ProductModel item : itemList) {
                if (item.getOwner().toLowerCase().contains(owner)) {
                    filteredList.add(item);
                }
            }
        } else if (!text.isEmpty() && !owner.isEmpty()) {
            text = text.toLowerCase();
            owner = owner.toLowerCase();
            for (ProductModel item : itemList) {
                if (item.getName().toLowerCase().contains(text) && item.getOwner().toLowerCase().contains(owner)) {
                    filteredList.add(item);
                }
            }
        }
        if (filteredList.isEmpty()){
            data_fl.setVisibility(View.GONE);
            no_data_fl.setVisibility(View.VISIBLE);
        }else{
            data_fl.setVisibility(View.VISIBLE);
            no_data_fl.setVisibility(View.GONE);
        }
        productAdapter.notifyDataSetChanged();
    }
}