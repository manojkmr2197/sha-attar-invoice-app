package com.app.sha.attar.invoice.activity;

import static com.app.sha.attar.invoice.utils.SharedConstants.ACCESSORIES_KEY;
import static com.app.sha.attar.invoice.utils.SharedConstants.PRODUCT_KEY;
import static com.app.sha.attar.invoice.utils.SharedConstants.SHA_ATTAR;
import static java.lang.Boolean.TRUE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.app.sha.attar.invoice.adapter.AccessoriesViewAdapter;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.utils.DBUtil;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class AccessoriesActivity extends AppCompatActivity implements View.OnClickListener {

    FrameLayout data_fl, no_data_fl;
    Context context;
    Activity activity;

    List<AccessoriesModel> itemList = new ArrayList<>();
    List<AccessoriesModel> filteredList = new ArrayList<>();
    RecyclerView recyclerView;
    ClickListener listener;

    AccessoriesViewAdapter adapter;

    TextInputEditText search_et;
    Spinner ownerSpinner,dealerSpinner;

    List<String> dealerList = new ArrayList<>();

    String searchText, searchOwner,searchDealer;

    DBUtil dbObj;
    SharedPrefHelper sharedPrefHelper;
    FirebaseFirestore db;

    ArrayAdapter<String> dealerAdapter;

    private SharedPreferences sharedPreferences;
    private Gson gson;

    private static final int REQUEST_WRITE_PERMISSION = 786;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_accessories);

        context = AccessoriesActivity.this;
        activity = AccessoriesActivity.this;

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }

        no_data_fl = (FrameLayout) findViewById(R.id.accessories_no_data_ll);
        data_fl = (FrameLayout) findViewById(R.id.accessories_data_ll);

        recyclerView = (RecyclerView) findViewById(R.id.accessories_recyclerView);

        FloatingActionButton add_fab = (FloatingActionButton) findViewById(R.id.accessories_add_fab);
        add_fab.setOnClickListener(this);

        TextView back = (TextView) findViewById(R.id.accessories_back);
        back.setOnClickListener(this);

        search_et = (TextInputEditText) findViewById(R.id.accessories_search_et);
        ownerSpinner = (Spinner) findViewById(R.id.accessories_spinner);
        dealerSpinner = (Spinner) findViewById(R.id.accessories_spinner_dealer);

        dbObj = new DBUtil();
        sharedPrefHelper = new SharedPrefHelper(context);
        db = DBUtil.getInstance();

        TextView download = (TextView) findViewById(R.id.accessories_download);
        download.setOnClickListener(this);

        sharedPreferences = context.getSharedPreferences(SHA_ATTAR, Context.MODE_PRIVATE);
        gson = new Gson();

        listener = new ClickListener() {
            @Override
            public void click(int index) {
                createDialogBox(context, filteredList.get(index));
            }
        };

        adapter = new AccessoriesViewAdapter(context, filteredList, listener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        search_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.v("data1 -- >", search_et.getText().toString());
                Log.v("data2 -- >", ownerSpinner.getSelectedItem().toString());
                Log.v("data3 -- >", dealerSpinner.getSelectedItem().toString());

                searchText = search_et.getText().toString();
                searchOwner = ownerSpinner.getSelectedItem().toString();
                if (searchOwner.equalsIgnoreCase("ALL")) {
                    searchOwner = "";
                }
                searchDealer = dealerSpinner.getSelectedItem().toString();
                if (searchDealer.equalsIgnoreCase("ALL")) {
                    searchDealer = "";
                }

                filter(searchText, searchOwner,searchDealer);
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
                filter(searchText, searchOwner,searchDealer);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dealerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v("data1 -- >", search_et.getText().toString());
                Log.v("data2 -- >", ownerSpinner.getSelectedItem().toString());
                Log.v("data3 -- >", dealerSpinner.getSelectedItem().toString());
                searchText = search_et.getText().toString();
                searchOwner = ownerSpinner.getSelectedItem().toString();
                if (searchOwner.equalsIgnoreCase("ALL")) {
                    searchOwner = "";
                }
                searchDealer = adapterView.getItemAtPosition(i).toString();
                if (searchDealer.equalsIgnoreCase("ALL")) {
                    searchDealer = "";
                }
                filter(searchText, searchOwner,searchDealer);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_items, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ownerSpinner.setAdapter(adapter);

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

    public void filter(String text, String owner,String dealer) {
        filteredList.clear();
        text = (text == null) ? "" : text;
        owner = (owner == null) ? "" : owner;
        dealer = (dealer == null) ? "" : dealer;
        if (text.isEmpty() && owner.isEmpty()) {
            filteredList.addAll(itemList);
        } else if (!text.isEmpty() && owner.isEmpty()) {
            text = text.toLowerCase();
            for (AccessoriesModel item : itemList) {
                if (item.getName().toLowerCase().contains(text)) {
                    filteredList.add(item);
                }
            }
        } else if (text.isEmpty() && !owner.isEmpty()) {
            owner = owner.toLowerCase();
            for (AccessoriesModel item : itemList) {
                if (item.getOwner().toLowerCase().contains(owner)) {
                    filteredList.add(item);
                }
            }
        } else if (!text.isEmpty() && !owner.isEmpty()) {
            text = text.toLowerCase();
            owner = owner.toLowerCase();
            for (AccessoriesModel item : itemList) {
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

        if (filteredList.isEmpty()) {
            data_fl.setVisibility(View.GONE);
            no_data_fl.setVisibility(View.VISIBLE);
        } else {
            data_fl.setVisibility(View.VISIBLE);
            no_data_fl.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if (R.id.accessories_back == view.getId()) {
            finish();
        } else if (R.id.accessories_add_fab == view.getId()) {
            createDialogBox(context, null);
        } else if (R.id.accessories_download == view.getId()){
            downloadAccessoriesList();
        }
    }

    private void downloadAccessoriesList() {
        try {
            saveExcelFile(itemList);
        } catch (Exception e) {
            Toast.makeText(this, "Report Generation failed ..!", Toast.LENGTH_LONG).show();
        }
    }

    private void saveExcelFile(List<AccessoriesModel> accessoriesModelList) throws Exception {

        String fileName = "accessories-" + System.currentTimeMillis() + ".xlsx";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.createAccessoriesExcelReport(accessoriesModelList, file);

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

    private void createDialogBox(Context context, AccessoriesModel accessoriesModel) {

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.dialog_accessories_create);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        TextInputEditText name = (TextInputEditText) dialog.findViewById(R.id.accessories_add_name);
        TextInputEditText price = (TextInputEditText) dialog.findViewById(R.id.accessories_add_price);
        TextInputEditText dealer = (TextInputEditText) dialog.findViewById(R.id.accessories_dealer_name);
        Spinner owner = (Spinner) dialog.findViewById(R.id.accessories_add_owner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_owners, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        owner.setAdapter(adapter);

        Button submit = (Button) dialog.findViewById(R.id.accessories_add_submit);
        TextView close = (TextView) dialog.findViewById(R.id.accessories_add_close);
        TextView delete = (TextView) dialog.findViewById(R.id.accessories_add_delete);
        if (accessoriesModel != null) {
            name.setText(accessoriesModel.getName());
            price.setText(String.valueOf(accessoriesModel.getPrice()));
            dealer.setText(accessoriesModel.getDealer());
            if ("MTS".equalsIgnoreCase(accessoriesModel.getOwner())) {
                owner.setSelection(0);
            } else if ("IK".equalsIgnoreCase(accessoriesModel.getOwner())) {
                owner.setSelection(1);
            }

            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Loading .! ", Toast.LENGTH_LONG).show();

                    db.collection(DatabaseConstants.ACCESSORIES_COLLECTION).document(accessoriesModel.getDocumentId())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Call the callback with null since the task was successful
                                    System.out.println("Accessories successfully deleted!");
                                    Toast.makeText(context, "Accessories successfully deleted!", Toast.LENGTH_LONG).show();
                                    setTotalAccessoriesItem();
                                    dialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while deleting Accessories. Please try again.!", Toast.LENGTH_LONG).show();
                                    System.err.println("Error deleting Accessories: " + e);
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
                    Toast.makeText(AccessoriesActivity.this, "Please enter Product name ..!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (StringUtils.isEmpty(price.getText().toString())) {
                    Toast.makeText(AccessoriesActivity.this, "Please enter Product Price ..!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(context, "Loading .! ", Toast.LENGTH_LONG).show();
                if (accessoriesModel != null) {
                    accessoriesModel.setName(name.getText().toString());
                    accessoriesModel.setPrice(Double.valueOf(price.getText().toString()));
                    accessoriesModel.setOwner(owner.getSelectedItem().toString());
                    accessoriesModel.setDealer(dealer.getText().toString());

                    db.collection(DatabaseConstants.ACCESSORIES_COLLECTION)
                            .document(accessoriesModel.getDocumentId())
                            .set(accessoriesModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "Update Accessories - " + accessoriesModel.getName() + " Successfully", Toast.LENGTH_LONG).show();
                                    System.out.println("Accessories Updated successfully.");
                                    setTotalAccessoriesItem();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while updating accessories. Please try again", Toast.LENGTH_LONG).show();
                                    System.out.println("Error while Updating accessories." + e);
                                }
                            });
                } else {
                    List<AccessoriesModel> filteredProducts = itemList.stream()
                            .filter(product -> name.getText().toString().equalsIgnoreCase(product.getName()))
                            .collect(Collectors.toList());

                    if(!filteredProducts.isEmpty()){
                        Toast.makeText(context,"Accessories Already Added .!",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AccessoriesModel accessoriesModel = new AccessoriesModel();
                    accessoriesModel.setName(name.getText().toString());
                    accessoriesModel.setPrice(Double.valueOf(price.getText().toString()));
                    accessoriesModel.setId(getLatestProductID());
                    accessoriesModel.setDocumentId(SingleTon.generateAccessoriesDocument());
                    accessoriesModel.setOwner(owner.getSelectedItem().toString());
                    accessoriesModel.setDealer(dealer.getText().toString());

                    db.collection(DatabaseConstants.ACCESSORIES_COLLECTION)
                            .document(accessoriesModel.getDocumentId())
                            .set(accessoriesModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "New Accessories - " + accessoriesModel.getName() + " Added", Toast.LENGTH_LONG).show();
                                    System.out.println("Accessories Added successfully.");
                                    setTotalAccessoriesItem();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while saving Accessories. Please try again", Toast.LENGTH_LONG).show();
                                    System.out.println("Error while saving Accessories." + e);
                                }
                            });

                }

                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private Integer getLatestProductID() {
        List<AccessoriesModel> accessories = sharedPrefHelper.getTotalAccessoriesList();

        if (accessories.isEmpty()) {
            return 1;
        } else {
            Optional<AccessoriesModel> maxProductModelrOptional = accessories.stream()
                    .max(Comparator.comparingInt(AccessoriesModel::getId));

            return maxProductModelrOptional.map(accessoriesModel -> accessoriesModel.getId() + 1).orElse(1);
        }
    }

    private void callApiData() {

        itemList = sharedPrefHelper.getTotalAccessoriesList();
        System.out.println("Number of Accessories-- " + itemList.size());
        if (itemList.isEmpty()) {
            no_data_fl.setVisibility(View.VISIBLE);
            data_fl.setVisibility(View.GONE);
            Toast.makeText(context, "Accessories is empty. Please try again .! ", Toast.LENGTH_LONG).show();
            return;
        }else{
            data_fl.setVisibility(View.VISIBLE);
            no_data_fl.setVisibility(View.GONE);
        }

        filteredList.clear();
        filteredList.addAll(itemList);
        adapter.notifyDataSetChanged();

        dealerList.clear();
        Set<String> data = new TreeSet<>();
        itemList.forEach(items ->{
            if(items.getDealer() != null){
                data.add(SingleTon.getDealerName(items.getDealer()));
            }
        });

        dealerList.add("ALL");
        dealerList.addAll(data);
        dealerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,  // Layout for the items
                dealerList  // The custom list of strings
        );

        dealerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dealerSpinner.setAdapter(dealerAdapter);


    }

    public void setTotalAccessoriesItem() {
        Toast.makeText(context, "Loading .! ", Toast.LENGTH_LONG).show();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        dbObj.getAllAccessories(new FirestoreCallback<List<AccessoriesModel>>() {
            @Override
            public void onCallback(List<AccessoriesModel> accessories) {
                System.out.println("Number of Accessories: " + accessories.size());
                String json = gson.toJson(accessories);
                editor.putString(ACCESSORIES_KEY, json);
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
            Toast.makeText(AccessoriesActivity.this, "Permission Granted .!", Toast.LENGTH_SHORT).show();
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