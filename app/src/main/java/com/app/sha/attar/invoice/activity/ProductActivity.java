package com.app.sha.attar.invoice.activity;

import static com.app.sha.attar.invoice.utils.SharedConstants.PRODUCT_KEY;
import static com.app.sha.attar.invoice.utils.SharedConstants.SHA_ATTAR;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Spinner;
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
import com.app.sha.attar.invoice.adapter.ProductViewAdapter;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.utils.DatabaseConstants;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.ReportGenerator;
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.app.sha.attar.invoice.utils.DBUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

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
    Spinner ownerSpinner,dealerSpinner,stockStatusSpinner;

    List<String> dealerList = new ArrayList<>();

    String searchText, searchOwner="ALL",searchDealer="ALL",searchStockStatus ="ALL";
    DBUtil dbObj;

    FirebaseFirestore db;

    SharedPrefHelper sharedPrefHelper;

    private SharedPreferences sharedPreferences;
    private Gson gson;

    private static final int REQUEST_WRITE_PERMISSION = 786;

    ArrayAdapter<String> dealerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_product);
        context = ProductActivity.this;
        activity = ProductActivity.this;

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        no_data_fl = (FrameLayout) findViewById(R.id.product_no_data_ll);
        data_fl = (FrameLayout) findViewById(R.id.product_data_ll);

        search_et = (TextInputEditText) findViewById(R.id.product_search_et);
        ownerSpinner = (Spinner) findViewById(R.id.product_spinner);
        dealerSpinner = (Spinner) findViewById(R.id.product_spinner_dealer);
        stockStatusSpinner = (Spinner) findViewById(R.id.product_spinner_availability);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_items, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ownerSpinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> stock_status_adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_stock_status, android.R.layout.simple_spinner_item);

        stock_status_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        stockStatusSpinner.setAdapter(stock_status_adapter);

        dealerList.add("ALL");
        dealerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,  // Layout for the items
                dealerList  // The custom list of strings
        );

        dealerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dealerSpinner.setAdapter(dealerAdapter);

        search_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                searchText = search_et.getText().toString();
                searchOwner = ownerSpinner.getSelectedItem().toString();
                if (searchOwner.equalsIgnoreCase("ALL")) {
                    searchOwner = "";
                }
                searchDealer = dealerSpinner.getSelectedItem().toString();
                if (searchDealer.equalsIgnoreCase("ALL")) {
                    searchDealer = "";
                }
                searchStockStatus = stockStatusSpinner.getSelectedItem().toString();
                if (searchStockStatus.equalsIgnoreCase("ALL")) {
                    searchStockStatus = "";
                }
                filter(searchText, searchOwner,searchDealer,searchStockStatus);
            }
        });

        ownerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                searchText = search_et.getText().toString();
                searchOwner = adapterView.getItemAtPosition(i).toString();
                if (searchOwner.equalsIgnoreCase("ALL")) {
                    searchOwner = "";
                }
                searchDealer = dealerSpinner.getSelectedItem().toString();
                if (searchDealer.equalsIgnoreCase("ALL")) {
                    searchDealer = "";
                }
                searchStockStatus = stockStatusSpinner.getSelectedItem().toString();
                if (searchStockStatus.equalsIgnoreCase("ALL")) {
                    searchStockStatus = "";
                }
                filter(searchText, searchOwner,searchDealer,searchStockStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dealerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                searchText = search_et.getText().toString();
                searchOwner = ownerSpinner.getSelectedItem().toString();
                if (searchOwner.equalsIgnoreCase("ALL")) {
                    searchOwner = "";
                }
                searchDealer = adapterView.getItemAtPosition(i).toString();
                if (searchDealer.equalsIgnoreCase("ALL")) {
                    searchDealer = "";
                }
                searchStockStatus = stockStatusSpinner.getSelectedItem().toString();
                if (searchStockStatus.equalsIgnoreCase("ALL")) {
                    searchStockStatus = "";
                }
                filter(searchText, searchOwner,searchDealer,searchStockStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        stockStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                searchText = search_et.getText().toString();
                searchOwner = ownerSpinner.getSelectedItem().toString();
                if (searchOwner.equalsIgnoreCase("ALL")) {
                    searchOwner = "";
                }
                searchDealer = dealerSpinner.getSelectedItem().toString();
                if (searchDealer.equalsIgnoreCase("ALL")) {
                    searchDealer = "";
                }
                searchStockStatus = adapterView.getItemAtPosition(i).toString();
                if (searchStockStatus.equalsIgnoreCase("ALL")) {
                    searchStockStatus = "";
                }
                filter(searchText, searchOwner,searchDealer,searchStockStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.product_recyclerView);
        TextView back = (TextView) findViewById(R.id.product_back);
        back.setOnClickListener(this);

        TextView download = (TextView) findViewById(R.id.product_download);
        download.setOnClickListener(this);

        FloatingActionButton add_fab = (FloatingActionButton) findViewById(R.id.product_add_fab);
        add_fab.setOnClickListener(this);
        dbObj = new DBUtil();
        sharedPrefHelper = new SharedPrefHelper(context);
        db = DBUtil.getInstance();
        sharedPreferences = context.getSharedPreferences(SHA_ATTAR, Context.MODE_PRIVATE);
        gson = new Gson();

        listener = new ClickListener() {
            @Override
            public void click(int index) {
                createDialogBox(context, filteredList.get(index));
            }
        };



        productAdapter = new ProductViewAdapter(context, filteredList, listener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(productAdapter);

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

        checkInternet();

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

    public void callApiData() {

        itemList = sharedPrefHelper.getTotalProductList();
        System.out.println("Number of products: " + itemList.size());
        if (itemList.isEmpty()) {
            Toast.makeText(context, "Products is empty. Please try again .! ", Toast.LENGTH_LONG).show();
            no_data_fl.setVisibility(View.VISIBLE);
            data_fl.setVisibility(View.GONE);
            return;
        } else {
            data_fl.setVisibility(View.VISIBLE);
            no_data_fl.setVisibility(View.GONE);
        }
        filteredList.clear();
        filteredList.addAll(itemList);
        dealerList.clear();
        Set<String> data = new TreeSet<>();
        itemList.forEach(items ->{
            if(items.getDealer() != null){
                data.add(SingleTon.getDealerName(items.getDealer()));
            }
        });

        dealerList.add("ALL");
        dealerList.addAll(data);
        dealerAdapter.notifyDataSetChanged();
        searchText = search_et.getText().toString();
        if (searchOwner.equalsIgnoreCase("ALL")) {
            searchOwner = "";
        }
        for (int i=0;i<dealerList.size();i++){
            if(searchDealer.equalsIgnoreCase(dealerList.get(i))){
                dealerSpinner.setSelection(i);
            }
        }

        if (searchDealer.equalsIgnoreCase("ALL")) {
            searchDealer = "";
        }

        if (searchStockStatus.equalsIgnoreCase("ALL")) {
            searchStockStatus = "";
        }
        filter(searchText, searchOwner,searchDealer,searchStockStatus);
        productAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if (R.id.product_back == view.getId()) {
            finish();
        } else if (R.id.product_add_fab == view.getId()) {
            createDialogBox(ProductActivity.this, null);
        } else if (R.id.product_download == view.getId()){
            downloadProductList();
        }
    }

    private void downloadProductList() {
        try {
            saveExcelFile(itemList);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Report Generation failed ..!", Toast.LENGTH_LONG).show();
        }
    }

    private void saveExcelFile(List<ProductModel> productModelList) throws Exception {
        String fileName = "product-" + System.currentTimeMillis() + ".xlsx";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.createProductExcelReport(productModelList, file);

        // Notify the user
        Toast.makeText(this, "Report Generated: " + fileName, Toast.LENGTH_LONG).show();

        // Use FileProvider to get the URI
        Uri fileUri = FileProvider.getUriForFile(this, "com.app.sha.attar.invoice.fileprovider", file);

        // Open the file using a file explorer
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.ms-excel");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }


    private void createDialogBox(Context context, ProductModel productModel) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_product_create);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setGravity(Gravity.TOP);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextInputEditText name = (TextInputEditText) dialog.findViewById(R.id.product_add_name);
        TextInputEditText dealer = (TextInputEditText) dialog.findViewById(R.id.product_dealer_name);
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
            dealer.setText(productModel.getDealer());
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
                    Toast.makeText(context, "Loading .! ", Toast.LENGTH_LONG).show();

                    db.collection(DatabaseConstants.PRODUCTS_COLLECTION).document(productModel.getDocumentId())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Call the callback with null since the task was successful
                                    Toast.makeText(context, "Product successfully deleted!", Toast.LENGTH_LONG).show();
                                    System.out.println("Product successfully deleted!");
                                    setTotalProductItem();
                                    dialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while deleting product. Please try again.!", Toast.LENGTH_LONG).show();
                                    System.err.println("Error while deleting product. " + e);
                                    dialog.dismiss();
                                }
                            });

                }
            });
        } else {
            delete.setVisibility(View.INVISIBLE);
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
                Toast.makeText(context, "Loading .! ", Toast.LENGTH_LONG).show();
                if (productModel != null) {
                    productModel.setName(name.getText().toString());
                    productModel.setPrice(price.getText().toString());
                    productModel.setDealer(dealer.getText().toString());
                    productModel.setOwner(owner.getSelectedItem().toString());
                    productModel.setStatus(available.isChecked() ? "Y" : "N");

                    db.collection(DatabaseConstants.PRODUCTS_COLLECTION)
                            .document(productModel.getDocumentId())
                            .set(productModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "Update product - " + productModel.getName() + " Successfully", Toast.LENGTH_LONG).show();
                                    System.out.println("Product Updated successfully.");
                                    setTotalProductItem();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while updating product. Please try again", Toast.LENGTH_LONG).show();
                                    System.out.println("Error while Updating product." + e);
                                }
                            });
                } else {

                    List<ProductModel> filteredProducts = itemList.stream()
                            .filter(product -> name.getText().toString().equalsIgnoreCase(product.getName()))
                            .collect(Collectors.toList());

                    if (!filteredProducts.isEmpty()) {
                        Toast.makeText(context, "Product Already Added .!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ProductModel newProductModel = new ProductModel();
                    newProductModel.setName(name.getText().toString());
                    newProductModel.setPrice(price.getText().toString());
                    newProductModel.setDealer(dealer.getText().toString());
                    newProductModel.setOwner(owner.getSelectedItem().toString());
                    newProductModel.setStatus(available.isChecked() ? "Y" : "N");
                    newProductModel.setCode(prepareProductCode(newProductModel.getName()));
                    newProductModel.setId(getLatestProductID());
                    newProductModel.setDocumentId(SingleTon.generateProductDocument());

                    db.collection(DatabaseConstants.PRODUCTS_COLLECTION)
                            .document(newProductModel.getDocumentId())
                            .set(newProductModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "New product - " + newProductModel.getName() + " Added", Toast.LENGTH_LONG).show();
                                    System.out.println("Product Added successfully.");
                                    setTotalProductItem();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while saving product. Please try again", Toast.LENGTH_LONG).show();
                                    System.out.println("Error while saving product." + e);
                                }
                            });

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
                    .collect(Collectors.groupingBy(productModel -> productModel.getName().charAt(0)));

            return String.valueOf(name.charAt(0)) +
                    ((groupedByFirstLetter.containsKey(name.charAt(0)) && groupedByFirstLetter.get(name.charAt(0)) != null) ?
                            verifyNewCode(groupedByFirstLetter.get(name.charAt(0)).size(), products, String.valueOf(name.charAt(0))) :
                            1);
        }

    }

    private int verifyNewCode(int i, List<ProductModel> products, String firstChar) {
        i = i + 1;
        for (ProductModel data : products) {
            if (data.getCode().equalsIgnoreCase(firstChar + i)) {
                i = verifyNewCode(i, products, firstChar);
            }
        }
        return i;
    }

    public void filter(String text, String owner,String dealer,String stockStatus) {
        Log.v("data1 -- >", text);
        Log.v("data2 -- >", owner);
        Log.v("data3 -- >", dealer);
        Log.v("data4 -- >", stockStatus);
        filteredList.clear();
        text = (text == null) ? "" : text;
        owner = (owner == null) ? "" : owner;
        dealer = (dealer == null) ? "" : dealer;
        stockStatus = (stockStatus ==null || stockStatus.equalsIgnoreCase("ALL"))? "": stockStatus;
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
        if(!filteredList.isEmpty() && !dealer.isEmpty()){
            for (int i = filteredList.size() - 1; i >= 0; i--) {
                if (!filteredList.get(i).getDealer().toLowerCase().contains(dealer.toLowerCase())) {
                    filteredList.remove(i);
                }
            }
        }

        if(!filteredList.isEmpty() && !stockStatus.isEmpty()){
            for (int i = filteredList.size() - 1; i >= 0; i--) {
                if (!filteredList.get(i).getStatus().equalsIgnoreCase((stockStatus.equalsIgnoreCase("Available"))?"Y":"N")) {
                    filteredList.remove(i);
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

    public void setTotalProductItem() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        dbObj.getProductDetails(new FirestoreCallback<List<ProductModel>>() {
            @Override
            public void onCallback(List<ProductModel> products) {
                System.out.println("Number of products-- " + products.size());
                String json = gson.toJson(products);
                editor.putString(PRODUCT_KEY, json);
                editor.apply();
                editor.commit();
                callApiData();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(ProductActivity.this, "Permission Granted .!", Toast.LENGTH_SHORT).show();
            // Permission granted, proceed with saving the file
//            try {
//                saveExcelFile(billingInvoiceModelList);
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(ReportActivity.this, "Internal Server Error. Please try again later.!", Toast.LENGTH_LONG).show();
//            }
        }
    }
}