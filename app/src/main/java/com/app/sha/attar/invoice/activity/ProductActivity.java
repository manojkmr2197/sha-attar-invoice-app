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
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.app.sha.attar.invoice.utils.DBUtil;

import org.apache.commons.lang3.StringUtils;

public class ProductActivity extends AppCompatActivity implements View.OnClickListener {

    FrameLayout data_fl, no_data_fl;
    Context context;
    Activity activity;

    List<ProductModel> itemList = new ArrayList<>();
    List<ProductModel> filteredList = new ArrayList<>();

    ProductViewAdapter productAdapter;
    RecyclerView recyclerView;
    ClickListener listener;

    TextInputEditText search_et;
    Spinner ownerSpinner;
    Button search_bt;

    String searchText, searchOwner;
    DBUtil dbObj;

    SharedPrefHelper sharedPrefHelper;

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
        sharedPrefHelper = new SharedPrefHelper(context);
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
        if (SingleTon.isNetworkConnected(activity)) {
            no_data_fl.setVisibility(View.GONE);
            data_fl.setVisibility(View.VISIBLE);
            callApiData();
        } else {
            no_data_fl.setVisibility(View.VISIBLE);
            data_fl.setVisibility(View.GONE);
            Toast.makeText(context, "No Internet connection. Please try again .! ", Toast.LENGTH_LONG).show();
        }

    }

    private void callApiData() {

        List<ProductModel> products = sharedPrefHelper.getTotalProductList();
        System.out.println("Number of products: " + itemList.size());
        if (products.isEmpty()) {
            Toast.makeText(context, "Internal Server Error. Please try again .! ", Toast.LENGTH_LONG).show();
        }
        itemList.clear();
        itemList.addAll(products);
        filteredList.clear();
        filteredList.addAll(itemList);
        productAdapter = new ProductViewAdapter(context, filteredList, listener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(productAdapter);
    }


    private void callReloadData() {
        sharedPrefHelper.setTotalProductItem();
        callApiData();
    }

    @Override
    public void onClick(View view) {
        if (R.id.product_back == view.getId()) {
            finish();
        } else if (R.id.product_add_fab == view.getId()) {
            createDialogBox(ProductActivity.this, null);
        } else if (R.id.product_search == view.getId()) {
            Log.v("data1 -- >", search_et.getText().toString());
            Log.v("data2 -- >", ownerSpinner.getSelectedItem().toString());

            searchText = search_et.getText().toString();
            searchOwner = ownerSpinner.getSelectedItem().toString();
            if (searchOwner.equalsIgnoreCase("ALL")) {
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
        TextView delete = (TextView) dialog.findViewById(R.id.product_add_delete);
        if (productModel != null) {
            name.setText(productModel.getName());
            price.setText(productModel.getPrice());
            if ("MTS".equalsIgnoreCase(productModel.getOwner())) {
                owner.setSelection(0);
            } else if ("IK".equalsIgnoreCase(productModel.getOwner())) {
                owner.setSelection(1);
            }
            if ("Y".equalsIgnoreCase(productModel.getStatus())) {
                available.setChecked(true);
            } else {
                available.setChecked(false);
            }
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dbObj.deleteProductById(productModel.getDocumentId(), new FirestoreCallback<Void>() {
                        @Override
                        public void onCallback(Void result) {
                            Toast.makeText(ProductActivity.this, "Product deleted (" + productModel.getName() + ") ..!", Toast.LENGTH_SHORT).show();
                            callReloadData();
                            dialog.dismiss();
                        }
                    });
                }
            });
        } else {
            delete.setVisibility(View.GONE);
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
                if (StringUtils.isEmpty(name.getText().toString())) {
                    Toast.makeText(ProductActivity.this, "Please enter Product name ..!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (StringUtils.isEmpty(price.getText().toString())) {
                    Toast.makeText(ProductActivity.this, "Please enter Product Price ..!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (productModel != null) {
                    productModel.setName(name.getText().toString());
                    productModel.setPrice(price.getText().toString());
                    productModel.setOwner(owner.getSelectedItem().toString());
                    productModel.setStatus(available.isChecked() ? "Y" : "N");

                    Boolean isUpdateSuccessfully = dbObj.updateProduct(productModel.getDocumentId(), productModel);
                    if (isUpdateSuccessfully == TRUE) {
                        Toast.makeText(ProductActivity.this, "Update Product ID - " + productModel.getId(), Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(ProductActivity.this, "Error while updating new product product - " + name.getText(), Toast.LENGTH_LONG).show();
                    }
                    callReloadData();
                } else {
                    ProductModel newProductModel = new ProductModel();
                    newProductModel.setName(name.getText().toString());
                    newProductModel.setPrice(price.getText().toString());
                    newProductModel.setOwner(owner.getSelectedItem().toString());
                    newProductModel.setStatus(available.isChecked() ? "Y" : "N");
                    newProductModel.setCode(prepareProductCode(newProductModel.getName()));
                    newProductModel.setId(getLatestProductID());

                    Boolean isSavedSuccessfully = dbObj.addProduct(newProductModel);
                    if (isSavedSuccessfully == TRUE) {
                        Toast.makeText(ProductActivity.this, "New product - " + name.getText() + " Added", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ProductActivity.this, "Error while adding new product product - " + name.getText(), Toast.LENGTH_LONG).show();
                    }
                    callReloadData();
                }

                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private Integer getLatestProductID() {
        List<ProductModel> products = sharedPrefHelper.getTotalProductList();

        if (products.isEmpty()) {
            return 1;
        } else {
            Optional<ProductModel> maxProductModelrOptional = products.stream()
                    .max(Comparator.comparingInt(ProductModel::getId));

            return maxProductModelrOptional.map(productModel -> productModel.getId() + 1).orElse(1);
        }
    }

    private String prepareProductCode(String name) {
        List<ProductModel> products = sharedPrefHelper.getTotalProductList();

        if (products.isEmpty()) {
            return String.valueOf(name.charAt(0)) + 1;
        } else {
            Map<Character, List<ProductModel>> groupedByFirstLetter = products.stream()
                    .collect(Collectors.groupingBy(user -> user.getName().charAt(0)));

            return (groupedByFirstLetter.containsKey(name.charAt(0)) && groupedByFirstLetter.get(name.charAt(0)) != null) ?
                    String.valueOf(name.charAt(0)) + groupedByFirstLetter.get(name.charAt(0)).size() + 1 :
                    String.valueOf(name.charAt(0)) + 1;
        }

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
        if (filteredList.isEmpty()) {
            data_fl.setVisibility(View.GONE);
            no_data_fl.setVisibility(View.VISIBLE);
        } else {
            data_fl.setVisibility(View.VISIBLE);
            no_data_fl.setVisibility(View.GONE);
        }
        productAdapter.notifyDataSetChanged();
    }
}